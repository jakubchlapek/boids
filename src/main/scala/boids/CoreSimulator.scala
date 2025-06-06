package boids

class CoreSimulator(
                     val worldWidth: Double,
                     val worldHeight: Double,
                     val boidsCount: Int,
                     val boidSize: Double,
                     val detectionRange: Double,
                     val maxForce: Double,
                     val maxSpeed: Double,
                     val minSpeed: Double,
                     val cohesionStrength: Double,
                     val alignmentStrength: Double,
                     val separationStrength: Double,
                     val separationRange: Double
                   ) {

  private val flockingBehavior = new FlockingBehavior(
    maxSpeed, maxForce, detectionRange,
    cohesionStrength, alignmentStrength,
    separationStrength, separationRange,
    worldWidth, worldHeight
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

  def update(): Unit = {
    val grid = spatialManager.buildGrid(allBoids)

    allBoids.foreach { boid =>
      val neighbors = spatialManager.findNeighbors(boid, grid)
      val closeNeighbors = spatialManager.findCloseNeighbors(boid, neighbors)

      val force = flockingBehavior.calculateFlockingForces(boid, grid, neighbors, closeNeighbors)
      boid.applyForce(force)
      boid.applyPhysics()
    }
  }
}
