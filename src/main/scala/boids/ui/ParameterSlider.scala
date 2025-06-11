package boids.ui

/** parameter with slider configuration */
case class ParameterSlider[T](
  name: String,
  title: String,
  initialValue: T,
  min: Double,
  max: Double,
  majorTickUnit: Double,
  blockIncrement: Double,
  formatPattern: String,
  updateAction: T => Double => Unit
) {
  def toSliderConfig: SliderConfig = {
    val doubleValue = initialValue match {
      case d: Double => d
      case i: Int => i.toDouble
      case _ => throw new IllegalArgumentException(s"Unsupported type for slider: ${initialValue.getClass.getName}")
    }

    SliderConfig(
      min,
      max,
      doubleValue,
      majorTickUnit,
      blockIncrement,
      formatPattern,
      name
    )
  }
}

/** factory for parameter sliders */
object ParameterSlider {
  // helper for double parameter slider
  def forDouble(
    name: String,
    title: String,
    initialValue: Double,
    min: Double,
    max: Double,
    majorTickUnit: Double,
    blockIncrement: Double,
    formatPattern: String,
    updateAction: Double => Unit
  ): ParameterSlider[Double] = {
    ParameterSlider(
      name,
      title,
      initialValue,
      min,
      max,
      majorTickUnit,
      blockIncrement,
      formatPattern,
      (value: Double) => (_: Double) => updateAction(value)
    )
  }

  // helper for integer parameter slider
  def forInt(
    name: String,
    title: String,
    initialValue: Int,
    min: Int,
    max: Int,
    majorTickUnit: Double,
    blockIncrement: Double,
    updateAction: Int => Unit
  ): ParameterSlider[Int] = {
    ParameterSlider(
      name,
      title,
      initialValue,
      min.toDouble,
      max.toDouble,
      majorTickUnit,
      blockIncrement,
      "%.0f",
      (value: Int) => (newValue: Double) => updateAction(newValue.toInt)
    )
  }
}
