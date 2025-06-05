class FlockingBehavior(
                        val maxSpeed: Double,
                        val maxForce: Double,
                        val detectionRange: Double,
                        val cohesionStrength: Double,
                        val alignmentStrength: Double,
                        val separationStrength: Double,
                        val separationDistance: Double,
                        val boundaryMargin: Double,
                        val boundaryForce: Double,
                        val worldWidth: Double,
                        val worldHeight: Double
                      ) {

  /** calculate all flocking forces for a boid and return the combined steering force */
  def calculateFlockingForces(boid: Boid, allBoids: Seq[Boid]): Point2D = {
    val neighbors = findNeighbors(boid, allBoids)
    val closeNeighbors = findCloseNeighbors(boid, neighbors)

    val cohesion = if (neighbors.nonEmpty) calculateCohesionForce(boid, neighbors) else Point2D(0, 0)
    val alignment = if (neighbors.nonEmpty) calculateAlignmentForce(boid, neighbors) else Point2D(0, 0)
    val separation = if (closeNeighbors.nonEmpty) calculateSeparationForce(boid, closeNeighbors) else Point2D(0, 0)
    val boundary = calculateBoundaryForce(boid.position)

    cohesion + alignment + separation + boundary
  }

  /** find all neighbors within detection range */
  def findNeighbors(boid: Boid, allBoids: Seq[Boid]): Seq[Boid] = {
    allBoids.filter(
      other => other != boid && other.position.distance(boid.position) < detectionRange
    )
  }

  /** find neighbors that are too close */
  def findCloseNeighbors(boid: Boid, neighbors: Seq[Boid]): Seq[Boid] = {
    neighbors.filter(
      neighbor => neighbor.position.distance(boid.position) < separationDistance
    )
  }

  /**calculate cohesion force - attraction to center of mass */
  def calculateCohesionForce(boid: Boid, neighbors: Seq[Boid]): Point2D = {
    val centerOfMass = neighbors.map(_.position).foldLeft(Point2D(0, 0))(_ + _) / neighbors.size.toDouble
    val desiredVector = centerOfMass - boid.position
    desiredVector.limit(maxForce) * cohesionStrength
  }

  /** calculate alignment force - match velocity with neighbors */
  def calculateAlignmentForce(boid: Boid, neighbors: Seq[Boid]): Point2D = {
    val averageVelocity = neighbors.map(_.velocity).reduce(_ + _) / neighbors.size
    val desiredVector = averageVelocity - boid.velocity
    desiredVector.limit(maxForce) * alignmentStrength
  }

  /** calculate separation force - avoid close neighbors */
  def calculateSeparationForce(boid: Boid, closeNeighbors: Seq[Boid]): Point2D = {
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

  /** calculate force repelling from the borders */
  def calculateBoundaryForce(position: Point2D): Point2D = {
    var force = Point2D(0, 0)

    if (position.x < boundaryMargin)
      force += Point2D(1, 0) * ((boundaryMargin - position.x) / boundaryMargin)
    else if (position.x > worldWidth - boundaryMargin)
      force += Point2D(-1, 0) * ((position.x - (worldWidth - boundaryMargin)) / boundaryMargin)

    if (position.y < boundaryMargin)
      force += Point2D(0, 1) * ((boundaryMargin - position.y) / boundaryMargin)
    else if (position.y > worldHeight - boundaryMargin)
      force += Point2D(0, -1) * ((position.y - (worldHeight - boundaryMargin)) / boundaryMargin)

    force * boundaryForce
  }
}
