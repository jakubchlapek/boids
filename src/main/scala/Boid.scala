import scalafx.scene.paint.Color
import scalafx.scene.shape.Polygon

case class Boid(var position: Point2D, var velocity: Point2D, size: Double = 10):
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
    velocity = velocity + force

  def seek(targetPoint: Point2D, maxForce: Double = 0.1): Point2D =
    val desired = targetPoint - position
    val steerForce = desired - velocity

    // Limit the maximum steering force
    val steerMagnitude = math.sqrt(steerForce.x * steerForce.x + steerForce.y * steerForce.y)
    if steerMagnitude > maxForce then
      steerForce / steerMagnitude * maxForce
    else
      steerForce
