package boids

type VoxelCoord = (Int, Int)

// voxel === grid cell
// recommended for voxelSize to be equal to or greater than detectionRange
class SpatialManager(val voxelSize: Double, val detectionRange: Double, val separationRange: Double):
  private val detectionRangeSquared: Double = detectionRange * detectionRange
  private val separationRangeSquared: Double = separationRange * separationRange
  
  /** update each boids voxelCoord, group them by voxelCoord*/
  def buildGrid(boids: Seq[Boid]): Map[VoxelCoord, Seq[Boid]] = {
    boids.foreach(boid => boid.voxelCoord = getVoxelCoord(boid.position))
    boids.groupBy(_.voxelCoord)
  }
  
  /** get voxelCoord for given Point2D */
  def getVoxelCoord(point: Point2D): VoxelCoord =
    ((point.x / voxelSize).toInt, (point.y / voxelSize).toInt)
  
  /** find all neighbors within detection range in surrounding voxels */
  def findNeighbors(boid: Boid, voxelGrid: Map[VoxelCoord, Seq[Boid]]): Seq[Boid] = {
    val (voxelX, voxelY) = boid.voxelCoord
  
    // checking surrounding cells
    val nearbyBoids = for {
      dx <- -1 to 1
      dy <- -1 to 1
      neighbors <- voxelGrid.get((voxelX + dx, voxelY + dy)).toSeq
    } yield neighbors
  
    nearbyBoids.flatten.filter(other =>
      other != boid && other.position.distanceSquared(boid.position) < detectionRangeSquared
    )
  }
  
  /** find neighbors that are too close */
  def findCloseNeighbors(boid: Boid, neighbors: Seq[Boid]): Seq[Boid] = {
    neighbors.filter(
      neighbor => neighbor.position.distanceSquared(boid.position) < separationRangeSquared
    )
  }