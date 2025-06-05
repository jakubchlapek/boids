import BoidsFx.{maxSpeed, minSpeed, worldHeight, worldWidth}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Polygon

case class Boid(var position: Point2D,
                var velocity: Point2D,
                var acceleration: Point2D = Point2D(0, 0),
                size: Double = 10):
  val shape: Polygon = new Polygon {
    points ++= Seq(size, 0.0, -size/2, size/2, -size/2, -size/2) // right-facing triangle
    fill = Color.White
    translateX = position.x
    translateY = position.y
    rotate = calculateRotation(velocity)
  }

  private def calculateRotation(vector: Point2D): Double =
    math.toDegrees(math.atan2(vector.y, vector.x))

  /** moves the boid and returns new point */
  def move(): Point2D =
    val newPosition = position + velocity
    shape.translateX = newPosition.x
    shape.translateY = newPosition.y

    // Update rotation based on velocity direction
    shape.rotate = calculateRotation(velocity)

    position = newPosition
    position

  def applyForce(force: Point2D): Unit =
    acceleration += force

  def seek(targetPoint: Point2D, maxForce: Double = 0.1): Point2D =
    val desired = targetPoint - position
    val steerForce = desired - velocity

    // Limit the maximum steering force
    val steerMagnitude = math.sqrt(steerForce.x * steerForce.x + steerForce.y * steerForce.y)
    if (steerMagnitude > maxForce)
      steerForce / steerMagnitude * maxForce
    else
      steerForce

  /** slows and turns boid around when he touches the borders */
  private def constrainToBoundaries(): Unit = {
    var newX = position.x
    var newY = position.y

    // x axis
    if (position.x < 0) newX = worldWidth
    else if (position.x > worldWidth) newX = 0

    // y axis
    if (position.y < 0) newY = worldHeight
    else if (position.y > worldHeight) newY = 0

    // if moved
    if (newX != position.x || newY != position.y) {
      position = Point2D(newX, newY)
      shape.translateX = position.x
      shape.translateY = position.y
    }
  }
  
  def applyPhysics(): Unit = {
    velocity += acceleration
    acceleration = Point2D(0, 0)
    velocity = velocity.limit(maxSpeed)
    position = move()
    constrainToBoundaries()
    updateColorBasedOnSpeed(minSpeed, maxSpeed)
  }

  def updateColorBasedOnSpeed(minSpeed: Double, maxSpeed: Double): Unit = {
    val speed = velocity.magnitude

    // (red for slow, green for medium, blue for fast)
    val normalizedSpeed = (speed - minSpeed) / (maxSpeed - minSpeed)
    val clampedSpeed = math.max(0.0, math.min(1.0, normalizedSpeed))

    val color = if (clampedSpeed < 0.5) {
      // red to green (through yellow)
      val ratio = clampedSpeed * 2
      Color.rgb(
        255,
        (255 * ratio).toInt,
        0
      )
    } else {
      // green to blue (through cyan)
      val ratio = (clampedSpeed - 0.5) * 2
      Color.rgb(
        (255 * (1 - ratio)).toInt,
        255,
        (255 * ratio).toInt
      )
    }

    shape.fill = color
  }

