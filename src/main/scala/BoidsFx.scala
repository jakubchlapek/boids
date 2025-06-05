import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.animation.AnimationTimer
import scalafx.scene.Group

object BoidsFx extends JFXApp3 {
  // config parameters
  val worldWidth: Double = 800
  val worldHeight: Double = 600
  val boidsCount: Int = 150

  val detectionRange: Double = 25
  val maxForce = 0.2 // max steering force
  val maxSpeed: Double = 2.0

  val cohesionStrength: Double = 0.01
  val alignmentStrength: Double = 0.03
  val separationStrength: Double = 0.2
  val separationDistance: Double = 10

  var allBoids: Seq[Boid] = Seq()



  def updateAllBoids(): Unit = {
    // for each boid:
    allBoids.foreach(boid => {
      // get all neighbors within range
      val neighbors: Seq[Boid] = allBoids.filter(
        nbor => nbor != boid && nbor.position.distance(boid.position) < detectionRange
      )

      var steeringForce = Point2D(0, 0)

      if (neighbors.nonEmpty) {
        // COHESION
        // get centre of mass (mean pos) of all neighbors
        val centerOfMass: Point2D = neighbors
          .map(_.position)
          .reduce(_ + _) / neighbors.size

        // calculate cohesion force (steering toward center of mass)
        val desiredCohesionPoint = centerOfMass - boid.position
        val cohesionForce = desiredCohesionPoint.limit(maxForce) * cohesionStrength

        // ALIGNMENT
        // calculate average velocity of neighbors
        val averageVelocity: Point2D = neighbors
          .map(_.velocity)
          .reduce(_ + _) / neighbors.size

        // steering force to align with average direction
        val desiredAlignmentPoint = averageVelocity - boid.velocity
        val alignmentForce = desiredAlignmentPoint.limit(maxForce) * alignmentStrength

        steeringForce = steeringForce + cohesionForce + alignmentForce
      }

      // SEPARATION
      // get neighbors within separation distance
      val tooCloseNeighbors: Seq[Boid] = neighbors.filter(nbor =>
        nbor.position.distance(boid.position) < separationDistance
      )

      if (tooCloseNeighbors.nonEmpty) {
        // vector pointing away
        val repulsionVector: Point2D = tooCloseNeighbors
          .map { nbor =>
            val diff = boid.position - nbor.position
            val distance = diff.magnitude
            if (distance != 0) diff / distance else Point2D(0, 0) // create a stronger force the closer neighbors are
          }
          .reduce(_ + _) / tooCloseNeighbors.size

        // add separation force
        val separationForce = repulsionVector.limit(maxForce) * separationStrength
        steeringForce = steeringForce + separationForce
      }

      // apply the combined steering force
      boid.applyForce(steeringForce)
      boid.velocity = boid.velocity.limit(maxSpeed)

      // Move the boid
      val newPos: Point2D = boid.move()

      // Wrap around screen boundaries
      val wrappedX = (newPos.x + worldWidth) % worldWidth
      val wrappedY = (newPos.y + worldHeight) % worldHeight

      boid.position = Point2D(wrappedX, wrappedY)
      boid.shape.translateX = wrappedX
      boid.shape.translateY = wrappedY
    })
  }


  def initializeBoids(rootGroup: Group, boidsCount: Int): Seq[Boid] = {
    // initiated at start
    val boids: Seq[Boid] = for (i <- 0 until boidsCount) yield {
      val x: Double = math.random() * worldWidth
      val y: Double = math.random() * worldHeight

      // Create random initial velocity
      val velX: Double = (math.random() * 2 - 1) * (maxSpeed / 4) // between -1 and 1
      val velY: Double = (math.random() * 2 - 1) * (maxSpeed / 4)
      val initialVelocity = Point2D(velX, velY)

      Boid(position = Point2D(x, y), velocity = initialVelocity, size=5)
    }
    rootGroup.children ++= boids.map(_.shape)
    boids
  }

  override def start(): Unit = {
    val rootGroup: Group = new Group()
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
