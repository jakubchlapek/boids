package boids.core

import boids.physics.{Vector2D, VoxelCoord}

trait BaseEntity {
  def position: Vector2D
  def velocity: Vector2D
  def voxelCoord: VoxelCoord
  def applyForce(force: Vector2D): Unit
  def applyPhysics(maxSpeed: Double, minSpeed: Double): Unit
}
