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
  val boidsCount: Int = 100
  val detectionRange: Double = 40
  val maxForce = 0.7 // max flocking force
  val maxSpeed: Double = 2.0
  val minSpeed: Double = maxSpeed / 5
  val cohesionStrength: Double = 0.02
  val alignmentStrength: Double = 0.08
  val separationStrength: Double = 0.8
  val separationDistance: Double = 20
  val boundaryMargin: Double = 50
  val boundaryForce: Double = 0.06

  val flockingBehavior = new FlockingBehavior(
    maxSpeed, maxForce, detectionRange,
    cohesionStrength, alignmentStrength,
    separationStrength, separationDistance,
    boundaryMargin, boundaryForce,
    worldWidth, worldHeight
  )
  var allBoids: Seq[Boid] = Seq()
  var detectionCircle: Circle = _
  var separationCircle: Circle = _

  def updateAllBoids(): Unit = {
    // for each boid:
    allBoids.foreach(boid => {
      // calculate the forces
      val force = flockingBehavior.calculateFlockingForces(boid, allBoids)
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
        radius = separationDistance
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