package boids.ui

import scalafx.geometry.Insets
import scalafx.scene.control.{Label, Slider, TitledPane}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font

/** reusable UI components */
object UIComponents {
  /** returns slider with given label and current value  */
  def createParameterSlider(config: SliderConfig, updateAction: Double => Unit): (Slider, Label) = {
    val slider = new Slider {
      min = config.min
      max = config.max
      value = config.initialValue
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = config.majorTickUnit
      blockIncrement = config.blockIncrement
    }

    val label = new Label {
      text = s"${config.propertyName}: ${config.initialValue.formatted(config.formatPattern)}"
      font = Font.font(12)
    }

    slider.value.onChange { (_, _, newVal) =>
      val value = newVal.doubleValue()
      label.text = s"${config.propertyName}: ${value.formatted(config.formatPattern)}"
      updateAction(value)
    }

    (slider, label)
  }

  /** returns TitledPane with given title */
  def createParameterPane(title: String, slider: Slider, label: Label): TitledPane = {
    new TitledPane {
      text = title
      content = new VBox(5) {
        children = Seq(label, slider)
        padding = Insets(5)
      }
      expanded = true
    }
  }

  /** helper to create slider and pane */
  def createParameterControl(title: String, config: SliderConfig, updateAction: Double => Unit): TitledPane = {
    val (slider, label) = createParameterSlider(config, updateAction)
    createParameterPane(title, slider, label)
  }
}