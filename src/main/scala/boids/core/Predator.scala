package boids.core

import boids.core.Boid
import boids.core.Predator
import boids.physics.{Point2D, VoxelCoord}

class Predator(
                position: Point2D,
                velocity: Point2D,
                voxelCoord: VoxelCoord,
                acceleration: Point2D = Point2D(0, 0),
                var huntingRange: Double,  // range for targeting boids
                speedMultiplier: Double  // predators move faster
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
             position: Point2D,
             velocity: Point2D,
             voxelCoord: VoxelCoord,
             acceleration: Point2D = Point2D(0, 0),
             huntingRange: Double,
             speedMultiplier: Double
           ): Predator = new Predator(
    position, velocity, voxelCoord, acceleration, huntingRange, speedMultiplier
  )
}
