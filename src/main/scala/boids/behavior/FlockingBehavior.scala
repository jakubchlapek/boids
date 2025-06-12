package boids.behavior

import boids.core.{Boid, Predator}
import boids.physics.{Vector2D, VoxelCoord}

class FlockingBehavior(
                        var maxForce: Double,
                        var cohesionStrength: Double,
                        var alignmentStrength: Double,
                        var separationStrength: Double,
                        var cursorInfluenceRange: Double,
                        var cursorInfluenceStrength: Double,
                        var predatorAvoidanceStrength: Double,
                        var predatorAvoidanceRange: Double,
                        var panicSpeedMultiplier: Double,
                      ) {
  /** calculate all flocking forces for a boid and return the combined steering force and isPanicking for boid */
  def calculateFlockingForces(boid: Boid,
                              voxelGrid: Map[VoxelCoord, Seq[Boid]],
                              neighbors: Seq[Boid],
                              closeNeighbors: Seq[Boid],
                              predators: Seq[Predator],
                              cursorPosition: Option[Vector2D] = None,
                              leftMousePressed: Boolean = false,
                              rightMousePressed: Boolean = false,
                              dragVector: Vector2D = Vector2D(0, 0)
                             ): (Vector2D, Boolean) = {
    if (neighbors.isEmpty && closeNeighbors.isEmpty) {
      return (Vector2D(0, 0), false)
    }

    val cohesion = if (neighbors.nonEmpty)
      calculateCohesionForce(boid, neighbors) else Vector2D(0, 0)
    val alignment = if (neighbors.nonEmpty)
      calculateAlignmentForce(boid, neighbors) else Vector2D(0, 0)
    val separation = if (closeNeighbors.nonEmpty)
      calculateSeparationForce(boid, closeNeighbors) else Vector2D(0, 0)
    val cursor = if (leftMousePressed || rightMousePressed)
      calculateCursorForce(boid, cursorPosition, leftMousePressed, rightMousePressed, dragVector) else Vector2D(0, 0)
    val (predatorAvoidance, isPanicking) = calculatePredatorAvoidanceForce(boid, predators)
    val boostForce = boid.velocity.normalize() * maxForce * 0.1

    (cohesion + alignment + separation + cursor + predatorAvoidance + boostForce, isPanicking)
  }

  /** calculate cohesion force - attraction to center of mass */
  private def calculateCohesionForce(boid: Boid, neighbors: Seq[Boid]): Vector2D = {
    val centerOfMass = neighbors.map(_.position).foldLeft(Vector2D(0, 0))(_ + _) / neighbors.size.toDouble
    val desiredVector = centerOfMass - boid.position
    desiredVector.limit(maxForce) * cohesionStrength
  }

  /** calculate alignment force - match velocity with neighbors */
  private def calculateAlignmentForce(boid: Boid, neighbors: Seq[Boid]): Vector2D = {
    if (neighbors.isEmpty) return Vector2D(0, 0)

    val averageVelocity = neighbors.map(_.velocity).foldLeft(Vector2D(0, 0))(_ + _) / neighbors.size
    val desiredVector = averageVelocity - boid.velocity
    desiredVector.limit(maxForce) * alignmentStrength
  }

  /** calculate separation force - avoid close neighbors */
  private def calculateSeparationForce(boid: Boid, closeNeighbors: Seq[Boid]): Vector2D = {
    if (closeNeighbors.isEmpty) return Vector2D(0, 0)

    val repulsionVector = closeNeighbors
      .map { neighbor =>
        val diff = boid.position - neighbor.position
        val distance = diff.magnitude
        if (distance > 0) diff / (distance * distance) else Vector2D(0, 0)
        // 1/d² creates stronger force for closer neighbors
      }
      .foldLeft(Vector2D(0, 0))(_ + _) / closeNeighbors.size

    repulsionVector.limit(maxForce) * separationStrength
  }

  private def calculateCursorForce(
                                    boid: Boid,
                                    cursorPosition: Option[Vector2D],
                                    leftPressed: Boolean,
                                    rightPressed: Boolean,
                                    dragVector: Vector2D
                                  ): Vector2D = {
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
          Some(Vector2D(0, 0))
        }
      } else {
        Some(Vector2D(0, 0))
      }
    }.getOrElse(Vector2D(0, 0))
  }

  def calculatePredatorAvoidanceForce(boid: Boid, predators: Seq[Predator]): (Vector2D, Boolean) = {
    if (predators.isEmpty) return (Vector2D(0, 0), false)

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
        Vector2D(0, 0)
      }
    }

    // Use foldLeft instead of reduce to handle empty collections safely
    val combinedForce = avoidanceForces.foldLeft(Vector2D(0, 0))(_ + _)
    (combinedForce, isPanicking)
  }

}
