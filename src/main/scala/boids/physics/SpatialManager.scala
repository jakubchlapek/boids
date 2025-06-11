package boids.physics

import boids.core.Boid

type VoxelCoord = (Int, Int)

// voxel === grid cell
// recommended for voxelSize to be equal to or greater than detectionRange
class SpatialManager(
                      var voxelSize: Double,
                      var detectionRange: Double,
                      var separationRange: Double,
                      var worldWidth: Double,
                      var worldHeight: Double
                    ):
  private var detectionRangeSquared: Double = detectionRange * detectionRange
  private var separationRangeSquared: Double = separationRange * separationRange

  def updateRanges(): Unit = {
    detectionRangeSquared = detectionRange * detectionRange
    separationRangeSquared = separationRange * separationRange
  }

  /** group boids by VoxelCoord */
  def buildGrid(boids: Seq[Boid]): Map[VoxelCoord, Seq[Boid]] = {
    boids.groupBy(_.voxelCoord)
  }

  /** get VoxelCoord for given Vector2D */
  def getVoxelCoord(point: Vector2D): VoxelCoord =
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

  /** moves a boid based on its velocity */
  def moveBoid(boid: Boid): Unit =
    val newPosition = boid.position + boid.velocity
    boid.position = newPosition

    // after moving, check if the boid's voxel coordinate has changed
    val newVoxelCoord = getVoxelCoord(boid.position)
    if (newVoxelCoord != boid.voxelCoord)
      boid.voxelCoord = newVoxelCoord

  /** constrains boids to world boundaries using wrap-around behavior */
  def constrainToBoundaries(boid: Boid): Unit =
    var newX = boid.position.x
    var newY = boid.position.y

    // x axis
    if (boid.position.x < 0) newX = worldWidth
    else if (boid.position.x > worldWidth) newX = 0

    // y axis
    if (boid.position.y < 0) newY = worldHeight
    else if (boid.position.y > worldHeight) newY = 0

    // if moved
    if (newX != boid.position.x || newY != boid.position.y)
      boid.position = Vector2D(newX, newY)
      // update voxel coordinate when position changes due to boundary wrapping
      boid.voxelCoord = getVoxelCoord(boid.position)

  /** combined movement and boundary checking in one operation */
  def updateBoidPosition(boid: Boid): Unit =
    moveBoid(boid)
    constrainToBoundaries(boid)
