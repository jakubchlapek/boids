package boids.core

import boids.core.Boid
import boids.*
import boids.behavior.{FlockingBehavior, PredatorBehavior}
import boids.physics.{Point2D, SpatialManager}
import boids.util.CursorState

class CoreSimulator(
                     var worldWidth: Double,
                     var worldHeight: Double,
                     var boidsCount: Int,
                     var boidSize: Double,
                     var detectionRange: Double,
                     var maxForce: Double,
                     var maxSpeed: Double,
                     var minSpeed: Double,
                     var cohesionStrength: Double,
                     var alignmentStrength: Double,
                     var separationStrength: Double,
                     var separationRange: Double,
                     var cursorInfluenceRange: Double,
                     var cursorInfluenceStrength: Double,
                     var predatorCount: Int = 2,
                     var predatorHuntingRange: Double = 50,
                     var predatorSpeedMultiplier: Double = 1.5,
                     var panicSpeedMultiplier: Double = 1.5,
                   ) {
  private var cursorState = CursorState(None, false, false)
  private var dragVector: Point2D = Point2D(0, 0)

  def setDragVector(vec: Point2D): Unit = {
    dragVector = vec
  }

  private val flockingBehavior: FlockingBehavior = new FlockingBehavior(
    maxForce = maxForce,
    cohesionStrength = cohesionStrength,
    alignmentStrength = alignmentStrength,
    separationStrength = separationStrength,
    cursorInfluenceRange = cursorInfluenceRange,
    cursorInfluenceStrength = cursorInfluenceStrength
  )

  private val predatorBehavior: PredatorBehavior = new PredatorBehavior()

  private val spatialManager: SpatialManager = new SpatialManager(
    voxelSize = detectionRange,
    detectionRange = detectionRange,
    separationRange = separationRange,
    worldWidth = worldWidth,
    worldHeight = worldHeight
  )

  var allBoids: Seq[Boid] = initializeBoids()
  var allPredators: Seq[Predator] = initializePredators()

  private def initializeBoids(): Seq[Boid] = {
    for (i <- 0 until boidsCount) yield {
      val x = math.random() * worldWidth
      val y = math.random() * worldHeight
      val velX = (math.random() * 2 - 1) * (maxSpeed / 4)
      val velY = (math.random() * 2 - 1) * (maxSpeed / 4)
      val initialVelocity = Point2D(velX, velY)

      Boid(
        position = Point2D(x, y),
        velocity = initialVelocity,
        voxelCoord = spatialManager.getVoxelCoord(Point2D(x, y))
      )
    }
  }

  private def initializePredators(): Seq[Predator] = {
    for (i <- 0 until predatorCount) yield {
      val x = math.random() * worldWidth
      val y = math.random() * worldHeight
      val velX = (math.random() * 2 - 1) * (maxSpeed / 2)
      val velY = (math.random() * 2 - 1) * (maxSpeed / 2)
      val initialVelocity = Point2D(velX, velY)

      Predator(
        position = Point2D(x, y),
        velocity = initialVelocity,
        voxelCoord = spatialManager.getVoxelCoord(Point2D(x, y)),
        huntingRange = predatorHuntingRange,
        speedMultiplier = predatorSpeedMultiplier
      )
    }
  }

  def updateCursorState(position: Option[Point2D],
                        leftPressed: Boolean,
                        rightPressed: Boolean): Unit = {
    cursorState = CursorState(position, leftPressed, rightPressed)
  }

  def updateSettings(): Unit = {
    // Update flocking behavior parameters
    flockingBehavior.maxForce = maxForce
    flockingBehavior.cohesionStrength = cohesionStrength
    flockingBehavior.alignmentStrength = alignmentStrength
    flockingBehavior.separationStrength = separationStrength
    flockingBehavior.cursorInfluenceRange = cursorInfluenceRange
    flockingBehavior.cursorInfluenceStrength = cursorInfluenceStrength
    flockingBehavior.panicSpeedMultiplier = panicSpeedMultiplier

    // Update spatial manager parameters
    spatialManager.voxelSize = detectionRange
    spatialManager.detectionRange = detectionRange
    spatialManager.separationRange = separationRange
    spatialManager.worldWidth = worldWidth
    spatialManager.worldHeight = worldHeight
    spatialManager.updateRanges()

    // Update predator behavior parameters
    predatorBehavior.maxForce = maxForce
  }

  /** reinitialize boids with current settings */
  def reinitializeBoids(): Unit = {
    allBoids = initializeBoids()
  }

  /** reinitialize predators with current settings */
  def reinitializePredators(): Unit = {
    allPredators = initializePredators()
  }

  def update(changeMade: Boolean): Unit = {
    if (changeMade)
      updateSettings()

    val grid = spatialManager.buildGrid(allBoids)

    allBoids.foreach { boid =>
      val neighbors = spatialManager.findNeighbors(boid, grid)
      val closeNeighbors = spatialManager.findCloseNeighbors(boid, neighbors)

      val (force, isPanicking) = flockingBehavior.calculateFlockingForces(
        boid, grid, neighbors, closeNeighbors, allPredators,
        cursorState.position, cursorState.leftPressed, cursorState.rightPressed, dragVector
      )
      boid.applyForce(force)

      if (!isPanicking)
        boid.applyPhysics(maxSpeed, minSpeed)
      else
        boid.applyPhysics(maxSpeed * flockingBehavior.panicSpeedMultiplier, minSpeed)

      spatialManager.updateBoidPosition(boid)
    }

    allPredators.foreach { predator =>
      val predatorForce = predatorBehavior.calculatePredatorForces(predator, allBoids)
      predator.applyForce(predatorForce)
      predator.applyPhysics(maxSpeed, minSpeed)
      spatialManager.updateBoidPosition(predator)
    }

  }
}
