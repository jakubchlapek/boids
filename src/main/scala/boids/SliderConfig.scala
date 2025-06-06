package boids

/** class for slider parameters */
case class SliderConfig(
                         min: Double,
                         max: Double,
                         initialValue: Double,
                         majorTickUnit: Double,
                         blockIncrement: Double,
                         formatPattern: String,
                         propertyName: String
                       )