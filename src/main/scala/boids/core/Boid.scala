package boids.core

import boids.Boid
import boids.physics.Point2D

class Boid(var position: Point2D,
                var velocity: Point2D,
                var voxelCoord: VoxelCoord,
                var acceleration: Point2D = Point2D(0, 0),
                val speedMultiplier: Double = 1.0
          ) extends SimulationEntity:

  def applyForce(force: Point2D): Unit =
    acceleration += force

  def applyPhysics(maxSpeed: Double, minSpeed: Double): Unit =
    velocity += acceleration
    acceleration = Point2D(0, 0)
    velocity = velocity.limit(maxSpeed * speedMultiplier)
    
    val speed = velocity.magnitude
    if (speed < minSpeed && speed > 0.0001) {
      velocity = velocity.normalize() * minSpeed
    }

object Boid {
  def apply(
             position: Point2D,
             velocity: Point2D,
             voxelCoord: VoxelCoord,
             acceleration: Point2D = Point2D(0, 0),
           ): Boid = new Boid(position, velocity, voxelCoord, acceleration)

}