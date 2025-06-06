import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.animation.AnimationTimer
import scalafx.scene.Group
import scalafx.scene.canvas.Canvas
import scalafx.scene.shape.Circle

object BoidsFx extends JFXApp3 {
  // config parameters
  val worldWidth: Double = 1200
  val worldHeight: Double = 800
  val boidsCount: Int = 5000
  val boidSize: Double = 4
  val detectionRange: Double = 30
  val maxForce = 0.7 // max flocking force
  val maxSpeed: Double = 2.0
  val minSpeed: Double = maxSpeed / 5
  val cohesionStrength: Double = 0.01
  val alignmentStrength: Double = 0.02
  val separationStrength: Double = 0.8
  val separationRange: Double = 10

  val flockingBehavior = new FlockingBehavior(
    maxSpeed, maxForce, detectionRange,
    cohesionStrength, alignmentStrength,
    separationStrength, separationRange,
    worldWidth, worldHeight
  )

  val spatialManager = new SpatialManager(
    voxelSize = detectionRange,
    detectionRange = detectionRange,
    separationRange = separationRange
  )

  var allBoids: Seq[Boid] = Seq()
  var detectionCircle: Circle = _
  var separationCircle: Circle = _

  private def updateAllBoids(): Unit = {
    val grid: Map[VoxelCoord, Seq[Boid]] = spatialManager.buildGrid(allBoids)

    // for each boid:
    allBoids.foreach(boid => {
      // get neighbors
      val neighbors: Seq[Boid] = spatialManager.findNeighbors(boid, grid)
      val closeNeighbors: Seq[Boid] = spatialManager.findCloseNeighbors(boid, neighbors)

      // calculate the forces
      val force: Point2D = flockingBehavior.calculateFlockingForces(boid, grid, neighbors, closeNeighbors)
      boid.applyForce(force)
      // move the boid
      boid.applyPhysics()
    })
  }

  private def initializeBoids(rootGroup: Group, boidsCount: Int): Seq[Boid] = {
    // initiated at start
    for (i <- 0 until boidsCount) yield {
      val x: Double = math.random() * worldWidth
      val y: Double = math.random() * worldHeight

      // create random initial velocity
      val velX: Double = (math.random() * 2 - 1) * (maxSpeed / 4) // between -1 and 1 times maxSpeed / 4
      val velY: Double = (math.random() * 2 - 1) * (maxSpeed / 4)
      val initialVelocity = Point2D(velX, velY)

      Boid(
        position = Point2D(x, y),
        velocity = initialVelocity,
        voxelCoord=spatialManager.getVoxelCoord(Point2D(x,y))
      )
    }
  }

  private def renderBoids(gc: scalafx.scene.canvas.GraphicsContext): Unit = {
    def drawBoidAsTriangle(boid: Boid): Unit = {
      val dir = boid.velocity.normalize()
      val perp = Point2D(-dir.y, dir.x) // perpendicular for triangle base

      val size = boidSize
      val baseWidth = size / 2

      // triangle tip (forward)
      val tip = boid.position + dir * size
      val baseLeft = boid.position - dir * (size * 0.3) + perp * baseWidth
      val baseRight = boid.position - dir * (size * 0.3) - perp * baseWidth

      gc.setFill(Color.White)
      gc.fillPolygon(
        Array(tip.x, baseLeft.x, baseRight.x),
        Array(tip.y, baseLeft.y, baseRight.y),
        3
      )
    }
    gc.clearRect(0, 0, worldWidth, worldHeight)

    // draw boids
    gc.setFill(Color.White)

    allBoids.foreach(drawBoidAsTriangle)

    // draw ranges
    if (allBoids.nonEmpty) {
      val first = allBoids.head

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

  override def start(): Unit = {
    val rootGroup: Group = new Group()
    val canvas = new Canvas(worldWidth, worldHeight)
    val gc = canvas.graphicsContext2D
    rootGroup.children.add(canvas)
    rootGroup.children.add(createLegend())

    allBoids = initializeBoids(rootGroup, boidsCount)

    stage = new PrimaryStage {
      title = "Boids Sim"
      width = worldWidth
      height = worldHeight
      scene = new Scene {
        fill = Color.Black
        content = rootGroup
      }
    }

    val timer = AnimationTimer {
      now =>
        // called each frame at 60 fps
        updateAllBoids()
        renderBoids(gc)
    }
    timer.start()
  }
}

def createLegend(): Group = {
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