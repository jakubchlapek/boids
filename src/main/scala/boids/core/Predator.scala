package boids.core

import boids.core.Boid
import boids.core.Predator
import boids.physics.{Vector2D, VoxelCoord}

class Predator(
                position: Vector2D,
                velocity: Vector2D,
                voxelCoord: VoxelCoord,
                acceleration: Vector2D = Vector2D(0, 0),
                speedMultiplier: Double // predators move faster
              ) extends Boid(position, velocity, voxelCoord, acceleration, speedMultiplier)

object Predator {
  def apply(
             position: Vector2D,
             velocity: Vector2D,
             voxelCoord: VoxelCoord,
             acceleration: Vector2D = Vector2D(0, 0),
             speedMultiplier: Double
           ): Predator = new Predator(
    position, velocity, voxelCoord, acceleration, speedMultiplier
  )
}
