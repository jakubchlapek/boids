case class Point2D(x: Double, y: Double):
  val magnitude: Double = math.sqrt(math.pow(x, 2) + math.pow(y, 2))
  
  def +(other: Point2D): Point2D = 
    Point2D(x + other.x, y + other.y)
    
  def -(other: Point2D): Point2D =
    Point2D(x - other.x, y - other.y)
    
  def *(scalar: Double): Point2D =
    Point2D(x * scalar, y * scalar)
  def *(other: Point2D): Point2D =
    Point2D(x * other.x, y * other.y)
    
  def /(scalar: Double): Point2D = 
    require(scalar != 0, "Division by zero")
    Point2D(x / scalar, y / scalar)

  def normalize(): Point2D =
    this / magnitude
    this
    
  def dot(other: Point2D): Double =
    x * other.x + y * other.y
    
  def distance(other: Point2D): Double =
    math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))

  /** return new vector, limited if the magnitude is over the max */
  def limit(max: Double): Point2D = {
    if (magnitude > max && magnitude > 0) {
      this * (max / magnitude)
    } else {
      this
    }
  }