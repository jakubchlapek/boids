package boids

case class CursorState(
                        position: Option[Point2D],
                        leftPressed: Boolean,
                        rightPressed: Boolean
                      )
