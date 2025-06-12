package boids.ui

import boids.ui.UIComponents.{createCategoryPane, createParameterControl}
import boids.ui.ParameterSlider
import boids.config.SimulationConfig
import boids.core.CoreSimulator
import boids.physics.Vector2D
import boids.util.CursorState
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Label, ScrollPane, Slider, TitledPane}
import scalafx.scene.input.MouseButton
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

object GUI extends JFXApp3 {
  // use default config for initial values
  private val defaultConfig = SimulationConfig.default

  // world dimensions
  private val initialWorldWidth: Double = defaultConfig.worldWidth
  private val initialWorldHeight: Double = defaultConfig.worldHeight

  // base entity parameters
  private var boidSize: Double                = defaultConfig.boidSize
  private var maxForce: Double                = defaultConfig.maxForce

  // boid parameters
  private var boidsCount: Int                 = defaultConfig.boidsCount
  private var detectionRange: Double          = defaultConfig.detectionRange
  private var maxSpeed: Double                = defaultConfig.maxSpeed
  private var minSpeed: Double                = defaultConfig.minSpeed
  private var panicSpeedMultiplier: Double    = defaultConfig.panicSpeedMultiplier

  // flocking behavior parameters
  private var cohesionStrength: Double        = defaultConfig.cohesionStrength
  private var alignmentStrength: Double       = defaultConfig.alignmentStrength
  private var separationStrength: Double      = defaultConfig.separationStrength
  private var separationRange: Double         = defaultConfig.separationRange
  private var predatorAvoidanceRange: Double  = defaultConfig.predatorAvoidanceRange
  private var avoidanceStrength: Double       = defaultConfig.avoidanceStrength

  // cursor interaction parameters
  private var cursorInfluenceRange: Double    = defaultConfig.cursorInfluenceRange
  private var cursorInfluenceStrength: Double = defaultConfig.cursorInfluenceStrength

  // predator parameters
  private var predatorCount: Int              = defaultConfig.predatorCount
  private var predatorHuntingRange: Double    = defaultConfig.predatorHuntingRange
  private var predatorSpeedMultiplier: Double = defaultConfig.predatorSpeedMultiplier
  private var huntingStrength: Double         = defaultConfig.huntingStrength
  private var wanderStrength: Double          = defaultConfig.wanderStrength

  private var cursorState: CursorState = CursorState(None, None, false, false)
  var changeMade = false
  private var simulation: CoreSimulator = _

  override def start(): Unit = {
    val canvas = new Canvas(initialWorldWidth, initialWorldHeight)
    val gc     = canvas.graphicsContext2D

    canvas.onMouseMoved = e => updateDragVector(Vector2D(e.x, e.y))
    canvas.onMouseDragged = e => updateDragVector(Vector2D(e.x, e.y))
    canvas.onMouseExited = _ =>
      cursorState = cursorState.copy(position = None, lastPosition = None)

    canvas.onMousePressed = e =>
      e.button match {
        case MouseButton.Primary   => cursorState = cursorState.copy(leftPressed = true)
        case MouseButton.Secondary => cursorState = cursorState.copy(rightPressed = true)
        case _                     =>
      }
    canvas.onMouseReleased = e =>
      e.button match {
        case MouseButton.Primary   => cursorState = cursorState.copy(leftPressed = false)
        case MouseButton.Secondary => cursorState = cursorState.copy(rightPressed = false)
        case _                     =>
      }

    val settingPanes: Seq[TitledPane] = getSettingPanes
    val sidebarContent = new VBox(10) {
      padding = Insets(10)
      children = settingPanes
      minWidth = 200
      maxWidth = 250
    }

    val rightSidebar = new ScrollPane {
      content = sidebarContent
      fitToWidth = true
      hbarPolicy = ScrollPane.ScrollBarPolicy.Never
      vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
      minWidth = 250
      maxWidth = 250
      prefWidth = 250
    }

    import scalafx.scene.layout.AnchorPane

    val canvasPane = new AnchorPane {
      children = canvas
      AnchorPane.setTopAnchor(canvas, 0.0)
      AnchorPane.setBottomAnchor(canvas, 0.0)
      AnchorPane.setLeftAnchor(canvas, 0.0)
      AnchorPane.setRightAnchor(canvas, 0.0)
    }

    val rootPane = new BorderPane {
      center = canvasPane
      right  = rightSidebar
    }

    stage = new PrimaryStage {
      title = "Boids Sim"
      maximized = true
      minWidth = 550
      minHeight = 400
      scene = new Scene {
        fill = Color.Black
        content = rootPane
      }
    }

    // create a simulation config based on default with current canvas dimensions
    val simulationConfig = defaultConfig.withWorldDimensions(
      canvas.width.value, 
      canvas.height.value
    )

    // Create the simulator with the config
    simulation = new CoreSimulator(simulationConfig)

    def updateCanvasSize(): Unit = {
      val sceneWidth = stage.width.value
      val sceneHeight = stage.height.value
      val sidebarWidth = rightSidebar.width.value

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

    // Update canvas size when sidebar width changes
    rightSidebar.width.addListener((obs, oldVal, newVal) => {
      updateCanvasSize()
    })

    stage.onShown = _ => {
      javafx.application.Platform.runLater { () =>
        updateCanvasSize()
      }
    }

    val timer = AnimationTimer { _ =>
      simulation.updateCursorState(cursorState.position, cursorState.leftPressed, cursorState.rightPressed)
      simulation.update(changeMade)
      changeMade = false
      renderBoids(gc)
    }
    timer.start()
  }


  /** define all parameter sliders in one place */
  private def defineParameterSliders(): Seq[ParameterSlider[_]] = {
    // Boid count
    val boidsCountSlider = ParameterSlider.forInt(
      "Boids Count",
      "Boids Count",
      boidsCount,
      100,
      5000,
      500,
      100,
      value => {
        boidsCount = value
        if (simulation != null) {
          // Update boid count and reinitialize boids
          simulation.boidsCount = boidsCount
          simulation.reinitializeBoids()
          changeMade = true
        }
      }
    )

    // Boid size
    val boidSizeSlider = ParameterSlider.forDouble(
      "Boid Size",
      "Boid Size",
      boidSize,
      1.0,
      10.0,
      1.0,
      0.5,
      "%.1f",
      value => {
        boidSize = value
        if (simulation != null) {
          // Update boidSize in the simulation
          simulation.boidSize = boidSize
          changeMade = true
        }
      }
    )

    // Detection range
    val detectionRangeSlider = ParameterSlider.forDouble(
      "Detection Range",
      "Detection Range",
      detectionRange,
      10.0,
      100.0,
      10.0,
      1.0,
      "%.1f",
      value => {
        detectionRange = value
        if (simulation != null) {
          simulation.detectionRange = detectionRange
          changeMade = true
        }
      }
    )

    // Max force
    val maxForceSlider = ParameterSlider.forDouble(
      "Max Force",
      "Max Force",
      maxForce,
      0.1,
      2.0,
      0.2,
      0.1,
      "%.1f",
      value => {
        maxForce = value
        if (simulation != null) {
          simulation.maxForce = maxForce
          changeMade = true
        }
      }
    )

    // Max speed
    val maxSpeedSlider = ParameterSlider.forDouble(
      "Max Speed",
      "Max / Min Speed",
      maxSpeed,
      0.1,
      5.0,
      1.0,
      0.2,
      "%.2f",
      value => {
        maxSpeed = value
        minSpeed = maxSpeed / 5.0
        if (simulation != null) {
          simulation.maxSpeed = maxSpeed
          simulation.minSpeed = minSpeed
          changeMade = true
        }
      }
    )

    // Cohesion strength
    val cohesionStrengthSlider = ParameterSlider.forDouble(
      "Cohesion Strength",
      "Cohesion Strength",
      cohesionStrength,
      0.0,
      0.5,
      0.1,
      0.005,
      "%.3f",
      value => {
        cohesionStrength = value
        if (simulation != null) {
          simulation.cohesionStrength = cohesionStrength
          changeMade = true
        }
      }
    )

    // Alignment strength
    val alignmentStrengthSlider = ParameterSlider.forDouble(
      "Alignment Strength",
      "Alignment Strength",
      alignmentStrength,
      0.0,
      0.5,
      0.1,
      0.005,
      "%.3f",
      value => {
        alignmentStrength = value
        if (simulation != null) {
          simulation.alignmentStrength = alignmentStrength
          changeMade = true
        }
      }
    )

    // Separation strength
    val separationStrengthSlider = ParameterSlider.forDouble(
      "Separation Strength",
      "Separation Strength",
      separationStrength,
      0.0,
      1.0,
      0.5,
      0.01,
      "%.2f",
      value => {
        separationStrength = value
        if (simulation != null) {
          simulation.separationStrength = separationStrength
          changeMade = true
        }
      }
    )

    // Separation range
    val separationRangeSlider = ParameterSlider.forDouble(
      "Separation Range",
      "Separation Range",
      separationRange,
      5.0,
      50.0,
      5.0,
      1.0,
      "%.1f",
      value => {
        separationRange = value
        if (simulation != null) {
          simulation.separationRange = separationRange
          changeMade = true
        }
      }
    )

    // Cursor influence range
    val cursorInfluenceRangeSlider = ParameterSlider.forDouble(
      "Cursor Influence Range",
      "Cursor Influence Range",
      cursorInfluenceRange,
      10.0,
      200.0,
      20.0,
      5.0,
      "%.1f",
      value => {
        cursorInfluenceRange = value
        if (simulation != null) {
          simulation.cursorInfluenceRange = cursorInfluenceRange
          changeMade = true
        }
      }
    )

    // Cursor influence strength
    val cursorInfluenceStrengthSlider = ParameterSlider.forDouble(
      "Cursor Influence Strength",
      "Cursor Influence Strength",
      cursorInfluenceStrength,
      0.0,
      1.0,
      0.2,
      0.05,
      "%.2f",
      value => {
        cursorInfluenceStrength = value
        if (simulation != null) {
          simulation.cursorInfluenceStrength = cursorInfluenceStrength
          changeMade = true
        }
      }
    )

    // Predator count
    val predatorCountSlider = ParameterSlider.forInt(
      "Predator Count",
      "Predator Count",
      predatorCount,
      0,
      10,
      1,
      1,
      value => {
        predatorCount = value
        if (simulation != null) {
          // Update predator count and reinitialize predators
          simulation.predatorCount = predatorCount
          simulation.reinitializePredators()
          changeMade = true
        }
      }
    )

    // Predator hunting range
    val predatorHuntingRangeSlider = ParameterSlider.forDouble(
      "Hunting Range",
      "Predator Hunting Range",
      predatorHuntingRange,
      10.0,
      100.0,
      10.0,
      20.0,
      "%.1f",
      value => {
        predatorHuntingRange = value
        if (simulation != null) {
          simulation.predatorHuntingRange = predatorHuntingRange
          changeMade = true
        }
      }
    )

    // Predator speed multiplier
    val predatorSpeedMultiplierSlider = ParameterSlider.forDouble(
      "Predator Speed",
      "Predator Speed Multiplier",
      predatorSpeedMultiplier,
      1.0,
      3.0,
      0.5,
      0.1,
      "%.1f",
      value => {
        predatorSpeedMultiplier = value
        if (simulation != null) {
          simulation.predatorSpeedMultiplier = predatorSpeedMultiplier
          changeMade = true
        }
      }
    )

    // Panic speed multiplier
    val panicSpeedMultiplierSlider = ParameterSlider.forDouble(
      "Panic Speed",
      "Panic Speed Multiplier",
      panicSpeedMultiplier,
      1.0,
      3.0,
      0.5,
      0.1,
      "%.1f",
      value => {
        panicSpeedMultiplier = value
        if (simulation != null) {
          simulation.panicSpeedMultiplier = panicSpeedMultiplier
          changeMade = true
        }
      }
    )

    // Hunting strength
    val huntingStrengthSlider = ParameterSlider.forDouble(
      "Hunting Strength",
      "Predator Hunting Strength",
      huntingStrength,
      0.1,
      3.0,
      0.2,
      0.1,
      "%.1f",
      value => {
        huntingStrength = value
        if (simulation != null) {
          simulation.huntingStrength = huntingStrength
          changeMade = true
        }
      }
    )

    // Wander strength
    val wanderStrengthSlider = ParameterSlider.forDouble(
      "Wander Strength",
      "Predator Wander Strength",
      wanderStrength,
      0.1,
      2.0,
      0.1,
      0.1,
      "%.1f",
      value => {
        wanderStrength = value
        if (simulation != null) {
          simulation.wanderStrength = wanderStrength
          changeMade = true
        }
      }
    )

    // Predator avoidance strength
    val avoidanceStrengthSlider = ParameterSlider.forDouble(
      "Predator Avoidance Strength",
      "Predator Avoidance Strength",
      avoidanceStrength,
      0.1,
      3.0,
      0.2,
      0.1,
      "%.1f",
      value => {
        avoidanceStrength = value
        if (simulation != null) {
          simulation.avoidanceStrength = avoidanceStrength
          changeMade = true
        }
      }
    )

    // Predator avoidance range
    val predatorAvoidanceRangeSlider = ParameterSlider.forDouble(
      "Avoidance Range",
      "Predator Avoidance Range",
      predatorAvoidanceRange,
      50.0,
      300.0,
      50.0,
      10.0,
      "%.1f",
      value => {
        predatorAvoidanceRange = value
        if (simulation != null) {
          simulation.predatorAvoidanceRange = predatorAvoidanceRange
          changeMade = true
        }
      }
    )

    // return all sliders
    Seq(
      // boid parameters
      boidsCountSlider,
      boidSizeSlider,
      detectionRangeSlider,
      maxForceSlider,
      maxSpeedSlider,
      panicSpeedMultiplierSlider,
      cohesionStrengthSlider,
      alignmentStrengthSlider,
      separationStrengthSlider,
      avoidanceStrengthSlider,
      separationRangeSlider,
      predatorAvoidanceRangeSlider,
      cursorInfluenceRangeSlider,
      cursorInfluenceStrengthSlider,

      // predator parameters
      predatorCountSlider,
      predatorHuntingRangeSlider,
      predatorSpeedMultiplierSlider,
      huntingStrengthSlider,
      wanderStrengthSlider
    )
  }

  private def getSettingPanes: Seq[TitledPane] = {
    // create parameter sliders
    val parameterSliders = defineParameterSliders()

    // split sliders into categories
    val baseEntitySliders = Seq(parameterSliders(1), parameterSliders(3))
    val boidSliders = Seq(
      parameterSliders.head,
      parameterSliders(2),
      parameterSliders(4),
      parameterSliders(5),
      parameterSliders(10),
      parameterSliders(11)
    )
    val flockingSliders = parameterSliders.slice(6, 10)
    val cursorSliders = parameterSliders.slice(12, 14)
    val predatorSliders = parameterSliders.slice(14, 19)

    Seq(
      createCategoryPane("Base Entity Config", baseEntitySliders),
      createCategoryPane("Boid Config", boidSliders),
      createCategoryPane("Predator Config", predatorSliders),
      createCategoryPane("Flocking Config", flockingSliders),
      createCategoryPane("Cursor Config", cursorSliders),
    )
  }

  private def updateDragVector(newPos: Vector2D): Unit = {
    cursorState.lastPosition match {
      case Some(last) if cursorState.leftPressed || cursorState.rightPressed =>
        val dragVector = newPos - last
        simulation.setDragVector(dragVector)
      case _ =>
        simulation.setDragVector(Vector2D(0, 0))
    }
    cursorState = cursorState.copy(position = Some(newPos), lastPosition = Some(newPos))
  }

  private def renderBoids(gc: scalafx.scene.canvas.GraphicsContext): Unit = {
    gc.setFill(Color(0, 0, 0, 0.2)) // transparent previous frame for trails
    gc.fillRect(0, 0, gc.canvas.width.value, gc.canvas.height.value)
    drawGrid(gc)

    simulation.allBoids.foreach { boid =>
      val dir = boid.velocity.normalize()
      val perp = Vector2D(-dir.y, dir.x)
      val size = simulation.boidSize
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
      val size = simulation.boidSize * 1.5 
      val dir = predator.velocity.normalize()
      val perp = Vector2D(-dir.y, dir.x)
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
        first.position.x - simulation.detectionRange,
        first.position.y - simulation.detectionRange,
        simulation.detectionRange * 2,
        simulation.detectionRange * 2
      )

      gc.setStroke(Color.Red)
      gc.strokeOval(
        first.position.x - simulation.separationRange,
        first.position.y - simulation.separationRange,
        simulation.separationRange * 2,
        simulation.separationRange * 2
      )
    }
    simulation.allPredators.headOption.foreach { first =>
      gc.setStroke(Color.Red)
      gc.setLineWidth(1)
      gc.strokeOval(
        first.position.x - simulation.predatorHuntingRange,
        first.position.y - simulation.predatorHuntingRange,
        simulation.predatorHuntingRange * 2,
        simulation.predatorHuntingRange * 2
      )
    }
  }

  private def drawGrid(gc: scalafx.scene.canvas.GraphicsContext): Unit = {
    val voxelSize = Seq(
      simulation.detectionRange,
      simulation.separationRange,
      simulation.predatorHuntingRange).max
    val width = gc.canvas.width.value
    val height = gc.canvas.height.value

    gc.setStroke(Color(0.5, 0.5, 0.5, 0.8))
    gc.setLineWidth(0.5)

    // vertical lines
    var x = 0.0
    while (x <= width) {
      gc.strokeLine(x, 0, x, height)
      x += voxelSize
    }

    // horizontal lines
    var y = 0.0
    while (y <= height) {
      gc.strokeLine(0, y, width, y)
      y += voxelSize
    }
  }

  private def getColorBySpeed(speed: Double): Color = {
    val normalizedSpeed = (speed - simulation.minSpeed) / (simulation.maxSpeed - simulation.minSpeed)
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
