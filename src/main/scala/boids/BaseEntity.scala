package boids

trait BaseEntity {
  def position: Point2D
  def velocity: Point2D
  def voxelCoord: VoxelCoord
  def applyForce(force: Point2D): Unit
  def applyPhysics(maxSpeed: Double, minSpeed: Double): Unit
}
