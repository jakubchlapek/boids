package boids.behavior

import boids.core.{Boid, Predator}
import boids.physics.{Point2D, VoxelCoord}

class FlockingBehavior(
                        var maxForce: Double,
                        var cohesionStrength: Double,
                        var alignmentStrength: Double,
                        var separationStrength: Double,
                        var cursorInfluenceRange: Double,
                        var cursorInfluenceStrength: Double = 100,
                        var predatorAvoidanceStrength: Double = 2.0,
                        var predatorAvoidanceRange: Double = 200.0,
                        var panicSpeedMultiplier: Double = 1.5,
                      ) {
  /** calculate all flocking forces for a boid and return the combined steering force and isPanicking for boid */
  def calculateFlockingForces( boid: Boid, 
                               voxelGrid: Map[VoxelCoord, Seq[Boid]], 
                               neighbors: Seq[Boid], 
                               closeNeighbors: Seq[Boid],
                               predators: Seq[Predator],
                               cursorPosition: Option[Point2D] = None,
                               leftMousePressed: Boolean = false,
                               rightMousePressed: Boolean = false,
                               dragVector: Point2D = Point2D(0, 0)
                             ): (Point2D, Boolean) = {
    if (neighbors.isEmpty && closeNeighbors.isEmpty) {
      return (Point2D(0, 0), false)
    }

    val cohesion = if (neighbors.nonEmpty)
      calculateCohesionForce(boid, neighbors) else Point2D(0, 0)
    val alignment = if (neighbors.nonEmpty)
      calculateAlignmentForce(boid, neighbors) else Point2D(0, 0)
    val separation = if (closeNeighbors.nonEmpty)
      calculateSeparationForce(boid, closeNeighbors) else Point2D(0, 0)
    val cursor = if (leftMousePressed || rightMousePressed)
      calculateCursorForce(boid, cursorPosition, leftMousePressed, rightMousePressed, dragVector) else Point2D(0, 0)
    val (predatorAvoidance, isPanicking) = calculatePredatorAvoidanceForce(boid, predators)

    (cohesion + alignment + separation + cursor + predatorAvoidance, isPanicking)
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

  def calculatePredatorAvoidanceForce(boid: Boid, predators: Seq[Predator]): (Point2D, Boolean) = {
    if (predators.isEmpty) return (Point2D(0, 0), false)

    var isPanicking = false
    val avoidanceForces = predators.map { predator =>
      val toPredator = boid.position - predator.position
      val distance = toPredator.magnitude

      if (distance < predatorAvoidanceRange) {
        // boid is panicking when a predator is nearby
        isPanicking = true

        // stronger avoidance for closer predators
        val avoidStrength = 1.0 - (distance / predatorAvoidanceRange)
        toPredator.normalize() * avoidStrength * avoidStrength * predatorAvoidanceStrength
      } else {
        Point2D(0, 0)
      }
    }

    (avoidanceForces.reduce(_ + _), isPanicking)
  }

}