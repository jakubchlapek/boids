import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.animation.AnimationTimer
import scalafx.scene.Group

type AngleRad = Double
extension (angle: AngleRad)
  def toDegrees: Double = angle * 180 / math.Pi
  def normalizeAngle: AngleRad = angle % (2 * math.Pi)

object BoidsFx extends JFXApp3 {
  // config parameters
  val worldWidth: Double = 800
  val worldHeight: Double = 600
  val boidsCount: Int = 300

  val detectionRange: Double = 25
  val maxTurnRate = 0.05 // radians per frame

  val cohesionStrength: Double = 0.03
  val alignmentStrength: Double = 0.07
  val separationStrength: Double = 0.2
  val separationDistance: Double = 10

  var allBoids: Seq[Boid] = Seq()

  def updateAllBoids(): Unit = {
    // for each boid:
    allBoids.foreach(boid => {
      // get all neighbors withing range
      val neighbors: Seq[Boid] = allBoids.filter(
        nbor => nbor != boid && nbor.position.distance(boid.position) < detectionRange
      )
      var steerAngleSum: AngleRad = 0.0
      if (neighbors.nonEmpty)
        // COHESION
        // get centre of mass (mean pos) of all neighbors
        val centre_of_mass: Point2D = neighbors
          .map(_.position)
          .reduce(_ + _) / neighbors.size
        // steer towards it
        val cohesionAngle: AngleRad = boid.angleToPoint(centre_of_mass)
        steerAngleSum += cohesionStrength * cohesionAngle

        // ALIGNMENT
        // steer towards average angle of neighbors
        val meanAngle: AngleRad = neighbors
          .map(_.angle)
          .sum / neighbors.size
        steerAngleSum += alignmentStrength * (meanAngle - boid.angle)

      // SEPARATION
      // get neighbors within separation distance
      val tooCloseNeighbors: Seq[Boid] = neighbors.filter(nbor =>
        nbor.position.distance(boid.position) < separationDistance
      )
      if (tooCloseNeighbors.nonEmpty) {
        // vector pointing away (average of differences)
        val repulsionVector: Point2D = tooCloseNeighbors
          .map(nbor => boid.position - nbor.position)
          .reduce(_ + _) / tooCloseNeighbors.size

        val targetPoint: Point2D = boid.position + repulsionVector
        val separationAngle: AngleRad = boid.angleToPoint(targetPoint)

        steerAngleSum += separationStrength * separationAngle
      }
      val turnAmount: AngleRad = -maxTurnRate max steerAngleSum.normalizeAngle min maxTurnRate
      boid.angle += turnAmount
      boid.shape.rotate = boid.angle.toDegrees

      // if outside boundaries then jump to the other side
      // TODO: figure out wrapping, once the boid fully cross
      val newPos: Point2D = boid.move()
      val wrappedX = (newPos.x + worldWidth) % worldWidth
      val wrappedY = (newPos.y + worldHeight) % worldHeight

      boid.position = Point2D(wrappedX, wrappedY)
    })
  }

  def initializeBoids(rootGroup: Group, boidsCount: Int): Seq[Boid] = {
    // initiated at start
    val boids: Seq[Boid] = for (i <- 0 until boidsCount) yield {
      val x: Double = math.random() * worldWidth
      val y: Double = math.random() * worldHeight
      val angle: AngleRad = math.random() * 2 * math.Pi
      Boid(position = Point2D(x, y), angle = angle, velocity = 0.5, size=5)
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