package boids

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
                     var cursorInfluenceStrength: Double
                   ) {
  private var cursorState = CursorState(None, false, false)
  private var dragVector: Point2D = Point2D(0, 0)

  def setDragVector(vec: Point2D): Unit = {
    dragVector = vec
  }

  private val flockingBehavior = new FlockingBehavior(
    maxSpeed, maxForce, detectionRange,
    cohesionStrength, alignmentStrength,
    separationStrength, separationRange,
    worldWidth, worldHeight,
    cursorInfluenceRange, cursorInfluenceStrength
  )

  private val spatialManager = new SpatialManager(
    voxelSize = detectionRange,
    detectionRange = detectionRange,
    separationRange = separationRange
  )

  var allBoids: Seq[Boid] = initializeBoids()

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

  def updateCursorState(position: Option[Point2D],
                        leftPressed: Boolean,
                        rightPressed: Boolean): Unit = {
    cursorState = CursorState(position, leftPressed, rightPressed)
  }

  def updateSettings(): Unit = {
    flockingBehavior.maxSpeed = maxSpeed
    flockingBehavior.maxForce = maxForce
    flockingBehavior.detectionRange = detectionRange
    flockingBehavior.cohesionStrength = cohesionStrength
    flockingBehavior.alignmentStrength = alignmentStrength
    flockingBehavior.separationStrength = separationStrength
    flockingBehavior.separationRange = separationRange
    flockingBehavior.cursorInfluenceRange = cursorInfluenceRange
    flockingBehavior.cursorInfluenceStrength = cursorInfluenceStrength
  }

  def update(changeMade: Boolean): Unit = {
    if (changeMade)
      updateSettings()
    val grid = spatialManager.buildGrid(allBoids)

    allBoids.foreach { boid =>
      val neighbors = spatialManager.findNeighbors(boid, grid)
      val closeNeighbors = spatialManager.findCloseNeighbors(boid, neighbors)

      val force = flockingBehavior.calculateFlockingForces(
        boid, grid, neighbors, closeNeighbors,
        cursorState.position, cursorState.leftPressed, cursorState.rightPressed, dragVector
      )
      boid.applyForce(force)
      boid.applyPhysics(maxSpeed, worldWidth, worldHeight)
    }
  }
}
