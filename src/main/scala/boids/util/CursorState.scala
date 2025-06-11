package boids.util

import boids.physics.Vector2D

case class CursorState(
                        position: Option[Vector2D],
                        lastPosition: Option[Vector2D],
                        leftPressed: Boolean,
                        rightPressed: Boolean
                      )
