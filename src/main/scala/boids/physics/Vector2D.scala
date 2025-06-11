package boids.physics

case class Vector2D(x: Double, y: Double):
  lazy val magnitude: Double = math.sqrt(math.pow(x, 2) + math.pow(y, 2))

  def +(other: Vector2D): Vector2D = 
    Vector2D(x + other.x, y + other.y)

  def -(other: Vector2D): Vector2D =
    Vector2D(x - other.x, y - other.y)
  def unary_- : Vector2D = Vector2D(-x, -y)

  def *(scalar: Double): Vector2D =
    Vector2D(x * scalar, y * scalar)
  def *(other: Vector2D): Vector2D =
    Vector2D(x * other.x, y * other.y)

  def /(scalar: Double): Vector2D = 
    require(scalar != 0, "Division by zero")
    Vector2D(x / scalar, y / scalar)

  def normalize(): Vector2D =
    if (magnitude > 0) this / magnitude else this


  def dot(other: Vector2D): Double =
    x * other.x + y * other.y

  def distance(other: Vector2D): Double =
    math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))

  /** distance between two points, without squaring */
  def distanceSquared(other: Vector2D): Double = {
    val dx = x - other.x
    val dy = y - other.y
    dx * dx + dy * dy
  }

  /** return new vector, limited if the magnitude is over the max */
  def limit(max: Double): Vector2D = {
    if (magnitude > max && magnitude > 0) {
      this * (max / magnitude)
    } else {
      this
    }
  }
