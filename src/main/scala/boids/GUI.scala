package boids

import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Label, Slider, TitledPane}
import scalafx.scene.input.MouseButton
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import boids.UIComponents._

object GUI extends JFXApp3 {
  private val initialWorldWidth: Double = 1200.0
  private val initialWorldHeight: Double = 800.0

  private var boidsCount: Int                 = 1000
  private var boidSize: Double                = 7.0
  private var detectionRange: Double          = 30.0
  private var maxForce: Double                = 0.7
  private var maxSpeed: Double                = 1.2
  private var minSpeed: Double                = maxSpeed / 5
  private var cohesionStrength: Double        = 0.01
  private var alignmentStrength: Double       = 0.02
  private var separationStrength: Double      = 0.5
  private var separationRange: Double         = 10.0
  private var cursorInfluenceRange: Double    = 75.0
  private var cursorInfluenceStrength: Double = 0.15

  private var leftMousePressed: Boolean       = false
  private var rightMousePressed: Boolean      = false
  private var lastCursorPosition: Option[Point2D] = None
  private var cursorPosition: Option[Point2D]     = None
  var changeMade = false
  private var simulation: CoreSimulator = _

  override def start(): Unit = {
    val canvas = new Canvas(initialWorldWidth, initialWorldHeight)
    val gc     = canvas.graphicsContext2D

    canvas.onMouseMoved = e => updateDragVector(Point2D(e.x, e.y))
    canvas.onMouseDragged = e => updateDragVector(Point2D(e.x, e.y))
    canvas.onMouseExited = _ =>
      cursorPosition = None

    canvas.onMousePressed = e =>
      e.button match {
        case MouseButton.Primary   => leftMousePressed = true
        case MouseButton.Secondary => rightMousePressed = true
        case _                     =>
      }
    canvas.onMouseReleased = e =>
      e.button match {
        case MouseButton.Primary   => leftMousePressed = false
        case MouseButton.Secondary => rightMousePressed = false
        case _                     =>
      }

    val settingPanes: Seq[TitledPane] = getSettingPanes
    val rightSidebar = new VBox(10) {
      padding = Insets(10)
      children = settingPanes
      minWidth = 200
      maxWidth = 250
    }

    val rootPane = new BorderPane {
      center = canvas
      right  = rightSidebar
    }

    stage = new PrimaryStage {
      title = "Boids Simulation with Sliders"
      maximized = true
      minWidth = 550
      minHeight = 400
      scene = new Scene {
        fill = Color.Black
        content = rootPane
      }
    }

    simulation = new CoreSimulator(
      canvas.width.value,
      canvas.height.value,
      boidsCount,
      boidSize,
      detectionRange,
      maxForce,
      maxSpeed,
      minSpeed,
      cohesionStrength,
      alignmentStrength,
      separationStrength,
      separationRange,
      cursorInfluenceRange,
      cursorInfluenceStrength
    )

    def updateCanvasSize(): Unit = {
      val sceneWidth = stage.width.value
      val sceneHeight = stage.height.value
      val sidebarWidth = rightSidebar.minWidth.value

      val minCanvasWidth = 300.0
      val requiredWidth = sidebarWidth + minCanvasWidth

      val availableWidth = if (sceneWidth >= requiredWidth) {
        sceneWidth - sidebarWidth
      } else {
        minCanvasWidth
      }
      val availableHeight = math.max(200, sceneHeight)

      // update canvas (boid space) size
      canvas.width = availableWidth
      canvas.height = availableHeight

      // update simulation world dimensions
      simulation.worldWidth = availableWidth
      simulation.worldHeight = availableHeight
      changeMade = true
    }

    stage.widthProperty.addListener((obs, oldVal, newVal) => {
      updateCanvasSize()
    })

    stage.heightProperty.addListener((obs, oldVal, newVal) => {
      updateCanvasSize()
    })

    stage.onShown = _ => {
      javafx.application.Platform.runLater { () =>
        updateCanvasSize()
      }
    }

    val timer = AnimationTimer { _ =>
      simulation.updateCursorState(cursorPosition, leftMousePressed, rightMousePressed)
      simulation.update(changeMade)
      changeMade = false
      renderBoids(gc)
    }
    timer.start()
  }


  private def getSettingPanes: Seq[TitledPane] = {
    // cohesion
    val cohesionPane = createParameterControl(
      "Cohesion Strength",
      SliderConfig(0.0, 0.5, cohesionStrength, 0.1, 0.005, "%.3f", "Cohesion"),
      value => {
        cohesionStrength = value
        if (simulation != null) {
          simulation.cohesionStrength = cohesionStrength
          changeMade = true
        }
      }
    )

    // alignment
    val alignmentPane = createParameterControl(
      "Alignment Strength",
      SliderConfig(0.0, 0.5, alignmentStrength, 0.1, 0.005, "%.3f", "Alignment"),
      value => {
        alignmentStrength = value
        if (simulation != null) {
          simulation.alignmentStrength = alignmentStrength
          changeMade = true
        }
      }
    )

    // separation
    val separationPane = createParameterControl(
      "Separation Strength",
      SliderConfig(0.0, 1.0, separationStrength, 0.5, 0.01, "%.2f", "Separation"),
      value => {
        separationStrength = value
        if (simulation != null) {
          simulation.separationStrength = separationStrength
          changeMade = true
        }
      }
    )

    // max speed
    val speedPane = createParameterControl(
      "Max / Min Speed",
      SliderConfig(0.1, 5.0, maxSpeed, 1.0, 0.2, "%.2f", "Max Speed"),
      value => {
        maxSpeed = value
        minSpeed = maxSpeed / 5.0
        if (simulation != null) {
          simulation.maxSpeed = maxSpeed
          simulation.minSpeed = minSpeed
        }
      }
    )

    // detection range
    val detectionRangePane = createParameterControl(
      "Detection Range",
      SliderConfig(10.0, 100.0, detectionRange, 10.0, 1.0, "%.1f", "Detection Range"),
      value => {
        detectionRange = value
        if (simulation != null) {
          simulation.detectionRange = detectionRange
          changeMade = true
        }
      }
    )

    // separation range
    val separationRangePane = createParameterControl(
      "Separation Range",
      SliderConfig(5.0, 50.0, separationRange, 5.0, 1.0, "%.1f", "Separation Range"),
      value => {
        separationRange = value
        if (simulation != null) {
          simulation.separationRange = separationRange
          changeMade = true
        }
      }
    )

    Seq(
      cohesionPane,
      alignmentPane,
      separationPane,
      speedPane,
      detectionRangePane,
      separationRangePane
    )
  }

  private def updateDragVector(newPos: Point2D): Unit = {
    lastCursorPosition match {
      case Some(last) if leftMousePressed || rightMousePressed =>
        val dragVector = newPos - last
        simulation.setDragVector(dragVector)
      case _ =>
        simulation.setDragVector(Point2D(0, 0))
    }
    cursorPosition = Some(newPos)
    lastCursorPosition = Some(newPos)
  }

  private def renderBoids(gc: scalafx.scene.canvas.GraphicsContext): Unit = {
    gc.clearRect(0, 0, gc.canvas.width.value, gc.canvas.height.value)

    simulation.allBoids.foreach { boid =>
      val dir = boid.velocity.normalize()
      val perp = Point2D(-dir.y, dir.x)
      val size = boidSize
      val baseWidth = size / 2

      val tip = boid.position + dir * size
      val baseLeft = boid.position - dir * (size * 0.3) + perp * baseWidth
      val baseRight = boid.position - dir * (size * 0.3) - perp * baseWidth

      gc.setFill(getColorBySpeed(boid.velocity.magnitude))
      gc.fillPolygon(
        Array(tip.x, baseLeft.x, baseRight.x),
        Array(tip.y, baseLeft.y, baseRight.y),
        3
      )
    }
    
    gc.fill = Color.Red
    simulation.allPredators.foreach { predator =>
      val size = boidSize * 1.5 
      val dir = predator.velocity.normalize()
      val perp = Point2D(-dir.y, dir.x)
      val baseWidth = size / 2

      val tip = predator.position + dir * size
      val baseLeft = predator.position - dir * (size * 0.3) + perp * baseWidth
      val baseRight = predator.position - dir * (size * 0.3) - perp * baseWidth

      gc.fillPolygon(
        Array(tip.x, baseLeft.x, baseRight.x),
        Array(tip.y, baseLeft.y, baseRight.y),
        3
      )
    }

    simulation.allBoids.headOption.foreach { first =>
      gc.setStroke(Color.LightBlue)
      gc.setLineWidth(1)
      gc.strokeOval(
        first.position.x - detectionRange,
        first.position.y - detectionRange,
        detectionRange * 2,
        detectionRange * 2
      )

      gc.setStroke(Color.Red)
      gc.strokeOval(
        first.position.x - separationRange,
        first.position.y - separationRange,
        separationRange * 2,
        separationRange * 2
      )
    }
  }

  private def getColorBySpeed(speed: Double): Color = {
    val normalizedSpeed = (speed - minSpeed) / (maxSpeed - minSpeed)
    val clampedSpeed = math.min(math.max(0.0, normalizedSpeed), 1.0)

    if (clampedSpeed < 0.5) {
      val t = clampedSpeed * 2
      Color(1.0, 1.0 - t, 1.0 - t, 1.0)
    } else {
      val t = (clampedSpeed - 0.5) * 2
      Color(t, t, 1.0, 1.0)
    }
  }
}
