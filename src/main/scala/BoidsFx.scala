import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.animation.AnimationTimer
import scalafx.scene.Group

object BoidsFx extends JFXApp3 {
  // config parameters
  val worldWidth: Double = 1200
  val worldHeight: Double = 800
  val boidsCount: Int = 150
  val detectionRange: Double = 50
  val maxForce = 0.2 // max steering force
  val maxSpeed: Double = 2.0
  val minSpeed: Double = maxSpeed / 5
  val cohesionStrength: Double = 0.03
  val alignmentStrength: Double = 0.08
  val separationStrength: Double = 0.3
  val separationDistance: Double = 20
  val boundaryMargin: Double = 200
  val boundaryForce: Double = 0.06

  val flockingBehavior = new FlockingBehavior(
    maxSpeed, maxForce, detectionRange,
    cohesionStrength, alignmentStrength,
    separationStrength, separationDistance,
    boundaryMargin, boundaryForce,
    worldWidth, worldHeight
  )
  var allBoids: Seq[Boid] = Seq()

  def updateAllBoids(): Unit = {
    // for each boid:
    allBoids.foreach(boid => {
      // calculate the forces
      val totalForce = flockingBehavior.calculateFlockingForces(boid, allBoids)
      // move the boid
      boid.applyPhysics(totalForce)
    })
  }

  def initializeBoids(rootGroup: Group, boidsCount: Int): Seq[Boid] = {
    // initiated at start
    val boids: Seq[Boid] = for (i <- 0 until boidsCount) yield {
      val x: Double = math.random() * (worldWidth - 2 * boundaryMargin) + boundaryMargin
      val y: Double = math.random() * (worldHeight - 2 * boundaryMargin) + boundaryMargin

      // create random initial velocity
      val velX: Double = (math.random() * 2 - 1) * (maxSpeed / 4) // between -1 and 1 times maxSpeed / 4
      val velY: Double = (math.random() * 2 - 1) * (maxSpeed / 4)
      val initialVelocity = Point2D(velX, velY)

      Boid(position = Point2D(x, y), velocity = initialVelocity, size=5)
    }
    rootGroup.children ++= boids.map(_.shape)
    boids
  }

  override def start(): Unit = {
    val rootGroup: Group = new Group()

    // boundaries
    val safeAreaBorder = new scalafx.scene.shape.Rectangle {
      x = boundaryMargin
      y = boundaryMargin
      width = worldWidth - 2 * boundaryMargin
      height = worldHeight - 2 * boundaryMargin
      fill = Color.Transparent
      stroke = Color.White
      strokeWidth = 1
      strokeDashArray = Seq(5, 5)
    }
    rootGroup.children.add(safeAreaBorder)

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
    }
    timer.start()
  }
}
