package boids.core

import boids.core.Boid
import boids.core.Predator
import boids.physics.{Vector2D, VoxelCoord}

class Predator(
                position: Vector2D,
                velocity: Vector2D,
                voxelCoord: VoxelCoord,
                acceleration: Vector2D = Vector2D(0, 0),
                var huntingRange: Double, // range for targeting boids
                speedMultiplier: Double // predators move faster
              ) extends Boid(position, velocity, voxelCoord, acceleration, speedMultiplier):

  def findTarget(boids: Seq[Boid]): Option[Boid] = {
    if (boids.isEmpty) return None

    val boidsInRange = boids.filter(b =>
      b.position.distanceSquared(position) < huntingRange * huntingRange
    )

    if (boidsInRange.isEmpty) return None

    // return the closest boid
    Some(boidsInRange.minBy(b => b.position.distanceSquared(position)))
  }

object Predator {
  def apply(
             position: Vector2D,
             velocity: Vector2D,
             voxelCoord: VoxelCoord,
             acceleration: Vector2D = Vector2D(0, 0),
             huntingRange: Double,
             speedMultiplier: Double
           ): Predator = new Predator(
    position, velocity, voxelCoord, acceleration, huntingRange, speedMultiplier
  )
}
