package boids.behavior

import boids.core.{Boid, Predator}
import boids.physics.Vector2D

class PredatorBehavior(
                        var maxForce: Double,
                        var huntingStrength: Double,
                        var wanderStrength: Double
                      ):
  /** force towards nearest boid */
  def calculateHuntingForce(predator: Predator, target: Boid): Vector2D = {
    val desiredDirection = target.position - predator.position
    desiredDirection.normalize() * maxForce * huntingStrength
  }

  /** force when without targets */
  def calculateWanderForce(predator: Predator): Vector2D = {
    // random wandering behavior with slight bias toward current direction
    val randomAngle = math.random() * math.Pi * 0.5 - math.Pi * 0.25
    val currentDirection = if (predator.velocity.magnitude > 0.001)
      predator.velocity.normalize()
    else
      Vector2D(math.random() * 2 - 1, math.random() * 2 - 1).normalize()

    val wanderForce = Vector2D(
      currentDirection.x * math.cos(randomAngle) - currentDirection.y * math.sin(randomAngle),
      currentDirection.x * math.sin(randomAngle) + currentDirection.y * math.cos(randomAngle)
    )

    wanderForce * wanderStrength
  }

  def calculatePredatorForces(predator: Predator, boids: Seq[Boid], target: Option[Boid]): Vector2D = {
    target match {
      case Some(tgt) =>
        calculateHuntingForce(predator, tgt)
      case None =>
        calculateWanderForce(predator)
    }
  }
