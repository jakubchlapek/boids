package boids.core

import boids.core.Boid
import boids.*
import boids.behavior.{FlockingBehavior, PredatorBehavior}
import boids.config.SimulationConfig
import boids.physics.{Vector2D, SpatialManager}
import boids.util.CursorState

class CoreSimulator(var config: SimulationConfig) {
  // Delegate properties to config
  def worldWidth: Double = config.worldWidth
  def worldWidth_=(value: Double): Unit = config = config.withWorldWidth(value)

  def worldHeight: Double = config.worldHeight
  def worldHeight_=(value: Double): Unit = config = config.withWorldHeight(value)

  def boidsCount: Int = config.boidsCount
  def boidsCount_=(value: Int): Unit = config = config.withBoidsCount(value)

  def boidSize: Double = config.boidSize
  def boidSize_=(value: Double): Unit = config = config.withBoidSize(value)

  def detectionRange: Double = config.detectionRange
  def detectionRange_=(value: Double): Unit = config = config.withDetectionRange(value)

  def maxForce: Double = config.maxForce
  def maxForce_=(value: Double): Unit = config = config.withMaxForce(value)

  def maxSpeed: Double = config.maxSpeed
  def maxSpeed_=(value: Double): Unit = config = config.withMaxSpeed(value)

  def minSpeed: Double = config.minSpeed
  def minSpeed_=(value: Double): Unit = config = config.withMinSpeed(value)

  def cohesionStrength: Double = config.cohesionStrength
  def cohesionStrength_=(value: Double): Unit = config = config.withCohesionStrength(value)

  def alignmentStrength: Double = config.alignmentStrength
  def alignmentStrength_=(value: Double): Unit = config = config.withAlignmentStrength(value)

  def separationStrength: Double = config.separationStrength
  def separationStrength_=(value: Double): Unit = config = config.withSeparationStrength(value)

  def separationRange: Double = config.separationRange
  def separationRange_=(value: Double): Unit = config = config.withSeparationRange(value)

  def cursorInfluenceRange: Double = config.cursorInfluenceRange
  def cursorInfluenceRange_=(value: Double): Unit = config = config.withCursorInfluenceRange(value)

  def cursorInfluenceStrength: Double = config.cursorInfluenceStrength
  def cursorInfluenceStrength_=(value: Double): Unit = config = config.withCursorInfluenceStrength(value)

  def predatorCount: Int = config.predatorCount
  def predatorCount_=(value: Int): Unit = config = config.withPredatorCount(value)

  def predatorHuntingRange: Double = config.predatorHuntingRange
  def predatorHuntingRange_=(value: Double): Unit = config = config.withPredatorHuntingRange(value)

  def predatorSpeedMultiplier: Double = config.predatorSpeedMultiplier
  def predatorSpeedMultiplier_=(value: Double): Unit = config = config.withPredatorSpeedMultiplier(value)

  def panicSpeedMultiplier: Double = config.panicSpeedMultiplier
  def panicSpeedMultiplier_=(value: Double): Unit = config = config.withPanicSpeedMultiplier(value)

  def huntingStrength: Double = config.huntingStrength
  def huntingStrength_=(value: Double): Unit = config = config.withHuntingStrength(value)

  def wanderStrength: Double = config.wanderStrength
  def wanderStrength_=(value: Double): Unit = config = config.withWanderStrength(value)

  def avoidanceStrength: Double = config.avoidanceStrength
  def avoidanceStrength_=(value: Double): Unit = config = config.withAvoidanceStrength(value)

  def predatorAvoidanceRange: Double = config.predatorAvoidanceRange
  def predatorAvoidanceRange_=(value: Double): Unit = config = config.withPredatorAvoidanceRange(value)
  private var cursorState = CursorState(None, None, false, false)
  private var dragVector: Vector2D = Vector2D(0, 0)

  def setDragVector(vec: Vector2D): Unit = {
    dragVector = vec
  }

  private val flockingBehavior: FlockingBehavior = new FlockingBehavior(
    maxForce = maxForce,
    cohesionStrength = cohesionStrength,
    alignmentStrength = alignmentStrength,
    separationStrength = separationStrength,
    cursorInfluenceRange = cursorInfluenceRange,
    cursorInfluenceStrength = cursorInfluenceStrength,
    predatorAvoidanceStrength = avoidanceStrength,
    predatorAvoidanceRange = predatorAvoidanceRange,
    panicSpeedMultiplier = panicSpeedMultiplier
  )

  private val predatorBehavior: PredatorBehavior = new PredatorBehavior(
    maxForce = maxForce,
    huntingStrength = huntingStrength,
    wanderStrength = wanderStrength
  )

  private val spatialManager: SpatialManager = new SpatialManager(
    voxelSize = detectionRange,
    detectionRange = detectionRange,
    separationRange = separationRange,
    huntingRange = predatorHuntingRange,
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
      val initialVelocity = Vector2D(velX, velY)

      Boid(
        position = Vector2D(x, y),
        velocity = initialVelocity,
        voxelCoord = spatialManager.getVoxelCoord(Vector2D(x, y))
      )
    }
  }

  private def initializePredators(): Seq[Predator] = {
    for (i <- 0 until predatorCount) yield {
      val x = math.random() * worldWidth
      val y = math.random() * worldHeight
      val velX = (math.random() * 2 - 1) * (maxSpeed / 2)
      val velY = (math.random() * 2 - 1) * (maxSpeed / 2)
      val initialVelocity = Vector2D(velX, velY)

      Predator(
        position = Vector2D(x, y),
        velocity = initialVelocity,
        voxelCoord = spatialManager.getVoxelCoord(Vector2D(x, y)),
        speedMultiplier = predatorSpeedMultiplier
      )
    }
  }

  def updateCursorState(position: Option[Vector2D],
                        leftPressed: Boolean,
                        rightPressed: Boolean): Unit = {
    cursorState = CursorState(position, cursorState.lastPosition, leftPressed, rightPressed)
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
    flockingBehavior.predatorAvoidanceStrength = avoidanceStrength
    flockingBehavior.predatorAvoidanceRange = predatorAvoidanceRange

    // Update spatial manager parameters
    spatialManager.voxelSize = Seq(detectionRange, separationRange, predatorHuntingRange).max
    spatialManager.detectionRange = detectionRange
    spatialManager.separationRange = separationRange
    spatialManager.huntingRange = predatorHuntingRange
    spatialManager.worldWidth = worldWidth
    spatialManager.worldHeight = worldHeight
    spatialManager.updateRanges()

    // Update predator behavior parameters
    predatorBehavior.maxForce = maxForce
    predatorBehavior.huntingStrength = huntingStrength
    predatorBehavior.wanderStrength = wanderStrength

    // Update predator parameters
    allPredators.foreach { predator =>
      predator.speedMultiplier = predatorSpeedMultiplier
    }
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
      val target: Option[Boid] = spatialManager.findTarget(predator, grid)
      val predatorForce = predatorBehavior.calculatePredatorForces(predator, allBoids, target)
      predator.applyForce(predatorForce)
      predator.applyPhysics(maxSpeed, minSpeed)
      spatialManager.updateBoidPosition(predator)
    }

  }
}
