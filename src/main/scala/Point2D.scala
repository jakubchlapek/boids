case class Point2D(x: Double, y: Double):
  def +(other: Point2D): Point2D = 
    Point2D(x + other.x, y + other.y)
    
  def -(other: Point2D): Point2D =
    Point2D(x - other.x, y - other.y)
    
  def *(scalar: Double): Point2D =
    Point2D(x * scalar, y * scalar)
    
  def /(scalar: Double): Point2D = 
    require(scalar != 0, "Division by zero")
    Point2D(x / scalar, y / scalar)
    
  def distance(other: Point2D): Double =
    math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))
    