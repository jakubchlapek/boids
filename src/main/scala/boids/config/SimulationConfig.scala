package boids.config

/**
 * configuration class for the boids simulation
 * holds all parameters used to configure the simulation
 */
case class SimulationConfig(
  // World parameters
  worldWidth: Double,
  worldHeight: Double,

  // Base Entity parameters
  boidSize: Double,
  maxForce: Double,

  // Boid parameters
  boidsCount: Int,
  detectionRange: Double,
  maxSpeed: Double,
  minSpeed: Double,
  panicSpeedMultiplier: Double,

  // Predator parameters
  predatorCount: Int,
  predatorHuntingRange: Double,
  predatorSpeedMultiplier: Double,
  huntingStrength: Double,
  wanderStrength: Double,

  // Flocking behavior parameters
  cohesionStrength: Double,
  alignmentStrength: Double,
  separationStrength: Double,
  separationRange: Double,
  avoidanceStrength: Double,
  predatorAvoidanceRange: Double,

  // Cursor interaction parameters
  cursorInfluenceRange: Double,
  cursorInfluenceStrength: Double
) {
  // Individual setters for each parameter
  def withWorldWidth(value: Double): SimulationConfig = withWorldDimensions(value, worldHeight)
  def withWorldHeight(value: Double): SimulationConfig = withWorldDimensions(worldWidth, value)

  def withBoidSize(value: Double): SimulationConfig = withBaseEntityParameters(size = value)
  def withMaxForce(value: Double): SimulationConfig = withBaseEntityParameters(force = value)

  def withBoidsCount(value: Int): SimulationConfig = withBoidParameters(count = value)
  def withDetectionRange(value: Double): SimulationConfig = withBoidParameters(detection = value)
  def withMaxSpeed(value: Double): SimulationConfig = withBoidParameters(maxSpd = value)
  def withMinSpeed(value: Double): SimulationConfig = withBoidParameters(minSpd = value)
  def withPanicSpeedMultiplier(value: Double): SimulationConfig = withBoidParameters(panicMultiplier = value)

  def withPredatorCount(value: Int): SimulationConfig = withPredatorParameters(count = value)
  def withPredatorHuntingRange(value: Double): SimulationConfig = withPredatorParameters(huntingRange = value)
  def withPredatorSpeedMultiplier(value: Double): SimulationConfig = withPredatorParameters(speedMultiplier = value)
  def withHuntingStrength(value: Double): SimulationConfig = withPredatorParameters(huntingStr = value)
  def withWanderStrength(value: Double): SimulationConfig = withPredatorParameters(wanderStr = value)

  def withCohesionStrength(value: Double): SimulationConfig = withFlockingParameters(cohesion = value)
  def withAlignmentStrength(value: Double): SimulationConfig = withFlockingParameters(alignment = value)
  def withSeparationStrength(value: Double): SimulationConfig = withFlockingParameters(separation = value)
  def withSeparationRange(value: Double): SimulationConfig = withFlockingParameters(sepRange = value)
  def withAvoidanceStrength(value: Double): SimulationConfig = withFlockingParameters(avoidance = value)
  def withPredatorAvoidanceRange(value: Double): SimulationConfig = withFlockingParameters(predatorAvoidRange = value)

  def withCursorInfluenceRange(value: Double): SimulationConfig = withCursorParameters(range = value)
  def withCursorInfluenceStrength(value: Double): SimulationConfig = withCursorParameters(strength = value)
  /**
   * creates a copy with updated world dimensions
   */
  def withWorldDimensions(width: Double, height: Double): SimulationConfig =
    copy(worldWidth = width, worldHeight = height)

  /**
   * creates a copy with updated base entity parameters
   */
  def withBaseEntityParameters(
    size: Double = boidSize,
    force: Double = maxForce
  ): SimulationConfig =
    copy(
      boidSize = size,
      maxForce = force
    )

  /**
   * creates a copy with updated boid parameters
   */
  def withBoidParameters(
    count: Int = boidsCount,
    detection: Double = detectionRange,
    maxSpd: Double = maxSpeed,
    minSpd: Double = minSpeed,
    panicMultiplier: Double = panicSpeedMultiplier
  ): SimulationConfig =
    copy(
      boidsCount = count,
      detectionRange = detection,
      maxSpeed = maxSpd,
      minSpeed = minSpd,
      panicSpeedMultiplier = panicMultiplier
    )

  /**
   * creates a copy with updated flocking behavior parameters
   */
  def withFlockingParameters(
    cohesion: Double = cohesionStrength,
    alignment: Double = alignmentStrength,
    separation: Double = separationStrength,
    sepRange: Double = separationRange,
    avoidance: Double = avoidanceStrength,
    predatorAvoidRange: Double = predatorAvoidanceRange
  ): SimulationConfig =
    copy(
      cohesionStrength = cohesion,
      alignmentStrength = alignment,
      separationStrength = separation,
      separationRange = sepRange,
      avoidanceStrength = avoidance,
      predatorAvoidanceRange = predatorAvoidRange
    )

  /**
   * creates a copy with updated cursor interaction parameters
   */
  def withCursorParameters(
    range: Double = cursorInfluenceRange,
    strength: Double = cursorInfluenceStrength
  ): SimulationConfig =
    copy(
      cursorInfluenceRange = range,
      cursorInfluenceStrength = strength
    )

  /**
   * creates a copy with updated predator parameters
   */
  def withPredatorParameters(
    count: Int = predatorCount,
    huntingRange: Double = predatorHuntingRange,
    speedMultiplier: Double = predatorSpeedMultiplier,
    huntingStr: Double = huntingStrength,
    wanderStr: Double = wanderStrength
  ): SimulationConfig =
    copy(
      predatorCount = count,
      predatorHuntingRange = huntingRange,
      predatorSpeedMultiplier = speedMultiplier,
      huntingStrength = huntingStr,
      wanderStrength = wanderStr
    )
}

/**
 * companion object for simulationconfig with default configuration
 */
object SimulationConfig {
  /**
   * creates a default configuration
   */
  def default: SimulationConfig = SimulationConfig(
    // World parameters
    worldWidth = 1200.0,
    worldHeight = 800.0,

    // Base Entity parameters
    boidSize = 5.0,
    maxForce = 0.4,

    // Boid parameters
    boidsCount = 1000,
    detectionRange = 40.0,
    maxSpeed = 1.5,
    minSpeed = 0.4,
    panicSpeedMultiplier = 1.5,

    // Predator parameters
    predatorCount = 2,
    predatorHuntingRange = 40.0,
    predatorSpeedMultiplier = 1.4,
    huntingStrength = 1.0,
    wanderStrength = 0.7,

    // Flocking behavior parameters
    cohesionStrength = 0.0005,
    alignmentStrength = 0.04,
    separationStrength = 0.7,
    separationRange = 15.0,
    avoidanceStrength = 1.5,
    predatorAvoidanceRange = 60.0,

    // Cursor interaction parameters
    cursorInfluenceRange = 100.0,
    cursorInfluenceStrength = 0.3
  )
}
