import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.animation.AnimationTimer
import scalafx.scene.Group
import scalafx.scene.shape.Circle

object BoidsFx extends JFXApp3 {
  // config parameters
  val worldWidth: Double = 1200
  val worldHeight: Double = 800
  val boidsCount: Int = 5000
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

    if (allBoids.nonEmpty) {
      val firstBoid = allBoids.head
      detectionCircle.centerX = firstBoid.position.x
      detectionCircle.centerY = firstBoid.position.y

      separationCircle.centerX = firstBoid.position.x
      separationCircle.centerY = firstBoid.position.y
    }
  }

  private def initializeBoids(rootGroup: Group, boidsCount: Int): Seq[Boid] = {
    // initiated at start
    val boids: Seq[Boid] = for (i <- 0 until boidsCount) yield {
      val x: Double = math.random() * worldWidth
      val y: Double = math.random() * worldHeight

      // create random initial velocity
      val velX: Double = (math.random() * 2 - 1) * (maxSpeed / 4) // between -1 and 1 times maxSpeed / 4
      val velY: Double = (math.random() * 2 - 1) * (maxSpeed / 4)
      val initialVelocity = Point2D(velX, velY)

      Boid(
        position = Point2D(x, y), 
        velocity = initialVelocity, 
        voxelCoord=spatialManager.getVoxelCoord(Point2D(x,y)), 
        size=2
      )
    }

    // visualization circles for boid
    if (boids.nonEmpty) {
      val firstBoid = boids.head

      firstBoid.shape.fill = Color.Purple

      detectionCircle = new Circle {
        centerX = firstBoid.position.x
        centerY = firstBoid.position.y
        radius = detectionRange
        fill = Color.Transparent
        stroke = Color.LightBlue
        strokeWidth = 1
        opacity = 0.6
      }

      separationCircle = new Circle {
        centerX = firstBoid.position.x
        centerY = firstBoid.position.y
        radius = separationRange
        fill = Color.Transparent
        stroke = Color.Red
        strokeWidth = 1
        opacity = 0.6
      }

      rootGroup.children.add(detectionCircle)
      rootGroup.children.add(separationCircle)
    }

    rootGroup.children ++= boids.map(_.shape)
    boids
  }

  override def start(): Unit = {
    val rootGroup: Group = new Group()

    allBoids = initializeBoids(rootGroup, boidsCount)

    rootGroup.children.add(createLegend())

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