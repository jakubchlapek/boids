package boids

import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.{Group, Scene}
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseButton
import scalafx.scene.paint.Color

object GUI extends JFXApp3 {
  val worldWidth: Double = 1200.0
  val worldHeight: Double = 800.0
  val boidsCount: Int = 5000
  val boidSize: Double = 4.0
  val detectionRange: Double = 30.0
  val maxForce: Double = 0.7
  val maxSpeed: Double = 2.0
  val minSpeed: Double = maxSpeed / 5
  val cohesionStrength: Double = 0.01
  val alignmentStrength: Double = 0.02
  val separationStrength: Double = 0.8
  val separationRange: Double = 10.0
  val cursorInfluenceRange: Double = 75.0
  val cursorInfluenceStrength: Double = 0.15

  private var leftMousePressed: Boolean = false
  private var rightMousePressed: Boolean = false
  private var lastCursorPosition: Option[Point2D] = None
  private var cursorPosition: Option[Point2D] = None

  private val simulation = new CoreSimulator(
    worldWidth, worldHeight, boidsCount, boidSize,
    detectionRange, maxForce, maxSpeed, minSpeed,
    cohesionStrength, alignmentStrength,
    separationStrength, separationRange,
    cursorInfluenceRange, cursorInfluenceStrength
  )

  override def start(): Unit = {
    val rootGroup = new Group()
    val canvas = new Canvas(worldWidth, worldHeight)
    val gc = canvas.graphicsContext2D

    canvas.onMouseMoved = e => updateDragVector(Point2D(e.x, e.y))
    canvas.onMouseDragged = e => updateDragVector(Point2D(e.x, e.y))

    canvas.onMouseExited = _ =>
      cursorPosition = None

    canvas.onMousePressed = e =>
      e.button match
        case MouseButton.Primary => leftMousePressed = true
        case MouseButton.Secondary => rightMousePressed = true
        case _ =>
    canvas.onMouseReleased = e =>
      e.button match
        case MouseButton.Primary => leftMousePressed = false
        case MouseButton.Secondary => rightMousePressed = false
        case _ =>

    rootGroup.children.add(canvas)
    rootGroup.children.add(createLegend())

    stage = new PrimaryStage {
      title = "Boids Simulation"
      width = worldWidth
      height = worldHeight
      scene = new Scene {
        fill = Color.Black
        content = rootGroup
      }
    }

    val timer = AnimationTimer { _ =>
      simulation.updateCursorState(cursorPosition, leftMousePressed, rightMousePressed)
      simulation.update()
      renderBoids(gc)
    }
    timer.start()
  }

  private def updateDragVector(newPos: Point2D): Unit = {
    lastCursorPosition match
      case Some(last) if leftMousePressed || rightMousePressed =>
        val dragVector = newPos - last
        simulation.setDragVector(dragVector)
      case _ =>
        simulation.setDragVector(Point2D(0, 0))
    cursorPosition = Some(newPos)
    lastCursorPosition = Some(newPos)
  }

  private def renderBoids(gc: scalafx.scene.canvas.GraphicsContext): Unit = {
    def drawBoidAsTriangle(boid: Boid): Unit = {
      val dir = boid.velocity.normalize()
      val perp = Point2D(-dir.y, dir.x)
      val size = boidSize
      val baseWidth = size / 2

      val tip = boid.position + dir * size
      val baseLeft = boid.position - dir * (size * 0.3) + perp * baseWidth
      val baseRight = boid.position - dir * (size * 0.3) - perp * baseWidth

      gc.setFill(getColorBySpeed(speed = boid.velocity.magnitude))
      gc.fillPolygon(
        Array(tip.x, baseLeft.x, baseRight.x),
        Array(tip.y, baseLeft.y, baseRight.y),
        3
      )
    }

    gc.clearRect(0, 0, worldWidth, worldHeight)

    simulation.allBoids.foreach(drawBoidAsTriangle)

    if (simulation.allBoids.nonEmpty) {
      val first = simulation.allBoids.head

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


  private def createLegend(): Group = {
    val legend = new Group

    val detectionLegendLine = new scalafx.scene.shape.Line {
      startX = 10; startY = 20; endX = 30; endY = 20
      stroke = Color.LightBlue; strokeWidth = 1
    }
    val detectionLegendText = new scalafx.scene.text.Text {
      x = 35; y = 24; text = "Detection Range"
      fill = Color.LightBlue; font = scalafx.scene.text.Font("Arial", 12)
    }
    val separationLegendLine = new scalafx.scene.shape.Line {
      startX = 10; startY = 40; endX = 30; endY = 40
      stroke = Color.Red; strokeWidth = 1
    }
    val separationLegendText = new scalafx.scene.text.Text {
      x = 35; y = 44; text = "Separation Distance"
      fill = Color.Red; font = scalafx.scene.text.Font("Arial", 12)
    }

    legend.children.addAll(
      detectionLegendLine, detectionLegendText,
      separationLegendLine, separationLegendText
    )
    legend
  }
}
