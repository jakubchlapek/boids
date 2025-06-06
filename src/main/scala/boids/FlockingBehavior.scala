package boids

class FlockingBehavior(
                        val maxSpeed: Double,
                        val maxForce: Double,
                        val detectionRange: Double,
                        val cohesionStrength: Double,
                        val alignmentStrength: Double,
                        val separationStrength: Double,
                        val separationRange: Double,
                        val worldWidth: Double,
                        val worldHeight: Double,
                        val cursorInfluenceRange: Double,
                        val cursorInfluenceStrength: Double = 100
                      ) {
  
  /** calculate all flocking forces for a boid and return the combined steering force */
  def calculateFlockingForces( boid: Boid, 
                               voxelGrid: Map[VoxelCoord, Seq[Boid]], 
                               neighbors: Seq[Boid], 
                               closeNeighbors: Seq[Boid],
                               cursorPosition: Option[Point2D] = None,
                               leftMousePressed: Boolean = false,
                               rightMousePressed: Boolean = false,
                               dragVector: Point2D = Point2D(0, 0)
                             ): Point2D = {
    if (neighbors.isEmpty && closeNeighbors.isEmpty) {
      return Point2D(0, 0)
    }

    val cohesion = if (neighbors.nonEmpty)
      calculateCohesionForce(boid, neighbors) else Point2D(0, 0)
    val alignment = if (neighbors.nonEmpty)
      calculateAlignmentForce(boid, neighbors) else Point2D(0, 0)
    val separation = if (closeNeighbors.nonEmpty)
      calculateSeparationForce(boid, closeNeighbors) else Point2D(0, 0)
    val cursor = if (leftMousePressed || rightMousePressed)
      calculateCursorForce(boid, cursorPosition, leftMousePressed, rightMousePressed, dragVector) else Point2D(0, 0)

    cohesion + alignment + separation + cursor
  }

  /** calculate cohesion force - attraction to center of mass */
  private def calculateCohesionForce(boid: Boid, neighbors: Seq[Boid]): Point2D = {
    val centerOfMass = neighbors.map(_.position).foldLeft(Point2D(0, 0))(_ + _) / neighbors.size.toDouble
    val desiredVector = centerOfMass - boid.position
    desiredVector.limit(maxForce) * cohesionStrength
  }

  /** calculate alignment force - match velocity with neighbors */
  private def calculateAlignmentForce(boid: Boid, neighbors: Seq[Boid]): Point2D = {
    val averageVelocity = neighbors.map(_.velocity).reduce(_ + _) / neighbors.size
    val desiredVector = averageVelocity - boid.velocity
    desiredVector.limit(maxForce) * alignmentStrength
  }

  /** calculate separation force - avoid close neighbors */
  private def calculateSeparationForce(boid: Boid, closeNeighbors: Seq[Boid]): Point2D = {
    val repulsionVector = closeNeighbors
      .map { neighbor =>
        val diff = boid.position - neighbor.position
        val distance = diff.magnitude
        if (distance > 0) diff / (distance * distance) else Point2D(0, 0)
        // 1/d² creates stronger force for closer neighbors
      }
      .reduce(_ + _) / closeNeighbors.size

    repulsionVector.limit(maxForce) * separationStrength
  }

  private def calculateCursorForce(
                                    boid: Boid,
                                    cursorPosition: Option[Point2D],
                                    leftPressed: Boolean,
                                    rightPressed: Boolean,
                                    dragVector: Point2D
                                  ): Point2D = {
    cursorPosition.flatMap { pos =>
      val toCursor = boid.position - pos
      val distance = toCursor.magnitude

      if (distance < cursorInfluenceRange) {
        val influence = 1.0 - (distance / cursorInfluenceRange)

        if (rightPressed) {
          // repel away from cursor
          val repelDirection = toCursor.normalize()
          val force = repelDirection * cursorInfluenceStrength * influence
          Some(force)

        } else if (leftPressed) {
          // push based on drag
          val scaledDrag = dragVector * (cursorInfluenceStrength * influence)
          Some(scaledDrag)

        } else {
          Some(Point2D(0, 0))
        }
      } else {
        Some(Point2D(0, 0))
      }
    }.getOrElse(Point2D(0, 0))
  }

  // previous approach
  //  /** calculate force repelling from the borders */
  //  def calculateBoundaryForce(position: Point2D, velocity: Point2D): Point2D = {
  //    var force = Point2D(0, 0)
  //
  //    for (dim <- 0 to 1) {
  //      val pos = if (dim == 0) position.x else position.y
  //      val vel = if (dim == 0) velocity.x else velocity.y
  //      val size = if (dim == 0) worldWidth else worldHeight
  //
  //      if (pos < boundaryMargin) {
  //        val distFactor = 1 - pos / boundaryMargin
  //        val velFactor = if (vel < 0) math.abs(vel) / maxSpeed * 0.5 else 0
  //        val dir = if (dim == 0) Point2D(1, 0) else Point2D(0, 1)
  //        force += dir * (distFactor + velFactor)
  //      }
  //      else if (pos > size - boundaryMargin) {
  //        val distFactor = 1 - (size - pos) / boundaryMargin
  //        val velFactor = if (vel > 0) math.abs(vel) / maxSpeed * 0.5 else 0
  //        val dir = if (dim == 0) Point2D(-1, 0) else Point2D(0, -1)
  //        force += dir * (distFactor + velFactor)
  //      }
  //    }
  //
  //    force * boundaryForce
  //  }
  //}
}