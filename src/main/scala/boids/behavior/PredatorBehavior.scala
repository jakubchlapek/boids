package boids.behavior

import boids.core.{Boid, Predator}
import boids.physics.Point2D

class PredatorBehavior(
                        var maxForce: Double = 1.0,
                        var huntingStrength: Double = 1.2,
                        var wanderStrength: Double = 0.5
                      ):
  /** force towards nearest boid */
  def calculateHuntingForce(predator: Predator, target: Boid): Point2D = {
    val desiredDirection = target.position - predator.position
    desiredDirection.normalize() * maxForce * huntingStrength
  }

  /** force when without targets */
  def calculateWanderForce(predator: Predator): Point2D = {
    // random wandering behavior with slight bias toward current direction
    val randomAngle = math.random() * math.Pi * 0.5 - math.Pi * 0.25
    val currentDirection = if (predator.velocity.magnitude > 0.001)
      predator.velocity.normalize()
    else
      Point2D(math.random() * 2 - 1, math.random() * 2 - 1).normalize()

    val wanderForce = Point2D(
      currentDirection.x * math.cos(randomAngle) - currentDirection.y * math.sin(randomAngle),
      currentDirection.x * math.sin(randomAngle) + currentDirection.y * math.cos(randomAngle)
    )

    wanderForce * wanderStrength
  }

  def calculatePredatorForces(predator: Predator, boids: Seq[Boid]): Point2D = {
    predator.findTarget(boids) match {
      case Some(target) =>
        calculateHuntingForce(predator, target)
      case None =>
        calculateWanderForce(predator)
    }
  }
