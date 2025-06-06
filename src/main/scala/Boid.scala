import BoidsFx.{maxSpeed, minSpeed, worldHeight, worldWidth}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Polygon

case class Boid(var position: Point2D,
                var velocity: Point2D,
                var voxelCoord: VoxelCoord,
                var acceleration: Point2D = Point2D(0, 0),
                size: Double = 10):

  /** moves the boid and returns new point */
  private def move(): Point2D =
    val newPosition = position + velocity
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
  private def constrainToBoundaries(): Unit =
    var newX = position.x
    var newY = position.y

    // x axis
    if (position.x < 0) newX = worldWidth
    else if (position.x > worldWidth) newX = 0

    // y axis
    if (position.y < 0) newY = worldHeight
    else if (position.y > worldHeight) newY = 0

    // if moved
    if (newX != position.x || newY != position.y)
      position = Point2D(newX, newY)

  
  def applyPhysics(): Unit =
    velocity += acceleration
    acceleration = Point2D(0, 0)
    velocity = velocity.limit(maxSpeed)
    position = move()
    constrainToBoundaries()
