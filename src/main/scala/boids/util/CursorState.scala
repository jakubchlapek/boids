package boids.util

import boids.physics.Point2D

case class CursorState(
                        position: Option[Point2D],
                        leftPressed: Boolean,
                        rightPressed: Boolean
                      )
