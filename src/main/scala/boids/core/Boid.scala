package boids.core

import boids.physics.{Vector2D, VoxelCoord}

class Boid(var position: Vector2D,
           var velocity: Vector2D,
           var voxelCoord: VoxelCoord,
           var acceleration: Vector2D = Vector2D(0, 0),
           var speedMultiplier: Double = 1.0
          ) extends BaseEntity:

  def applyForce(force: Vector2D): Unit =
    acceleration += force

  def applyPhysics(maxSpeed: Double, minSpeed: Double): Unit =
    velocity += acceleration
    acceleration = Vector2D(0, 0)
    velocity = velocity.limit(maxSpeed * speedMultiplier)

    val speed = velocity.magnitude
    if (speed < minSpeed && speed > 0.0001) {
      velocity = velocity.normalize() * minSpeed
    }

object Boid {
  def apply(
             position: Vector2D,
             velocity: Vector2D,
             voxelCoord: VoxelCoord,
             acceleration: Vector2D = Vector2D(0, 0),
           ): Boid = new Boid(position, velocity, voxelCoord, acceleration)

}
