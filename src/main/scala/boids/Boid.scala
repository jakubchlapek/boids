package boids

case class Boid(var position: Point2D,
                var velocity: Point2D,
                var voxelCoord: VoxelCoord,
                var acceleration: Point2D = Point2D(0, 0),
                size: Double = 10
               ):

  def applyForce(force: Point2D): Unit =
    acceleration += force

  def applyPhysics(maxSpeed: Double, minSpeed: Double): Unit =
    velocity += acceleration
    acceleration = Point2D(0, 0)
    velocity = velocity.limit(maxSpeed)
    
    val speed = velocity.magnitude
    if (speed < minSpeed && speed > 0.0001) {
      velocity = velocity.normalize() * minSpeed
    }
