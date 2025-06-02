import scalafx.scene.paint.Color
import scalafx.scene.shape.Polygon

case class Boid(var position: Point2D, var angle: AngleRad, var velocity: Double, size: Double = 10):
  val shape: Polygon = new Polygon {
    points ++= Seq(size, 0.0, -size/2, size/2, -size/2, -size/2) // right-facing triangle
    fill = Color.White
    translateX = position.x
    translateY = position.y
    rotate = angle.toDegrees
  }

  /** moves the boid and returns new point */
  def move(): Point2D =
    val dx: Double = velocity * math.cos(angle)
    val dy: Double = velocity * math.sin(angle)

    val newPosition = Point2D(position.x + dx, position.y + dy)
    shape.translateX = newPosition.x
    shape.translateY = newPosition.y
    position = newPosition
    position

  def angleToPoint(targetPoint: Point2D): Double =
    val dx: Double = targetPoint.x - position.x
    val dy: Double = targetPoint.y - position.y
    math.atan2(dy, dx)