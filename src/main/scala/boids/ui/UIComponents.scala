package boids.ui

import scalafx.geometry.Insets
import scalafx.scene.control.{Label, Slider, TitledPane}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font

/** reusable UI components */
object UIComponents {
  /** creates a category pane containing multiple parameter sliders */
  def createCategoryPane(title: String, paramSliders: Seq[ParameterSlider[_]]): TitledPane = {
    val controls = paramSliders.map { slider =>
      val config = slider.toSliderConfig
      val updateAction = slider.updateAction(slider.initialValue)
      val (sliderControl, label) = createParameterSlider(config, updateAction)
      new VBox(5) {
        children = Seq(label, sliderControl)
        padding = Insets(5)
      }
    }

    val pane = new TitledPane {
      text = title
      content = new VBox(10) {
        children = controls
        padding = Insets(5)
      }
      expanded = false
      animated = false
    }

    // force layout update when expanded/collapsed
    pane.expandedProperty().addListener((_, _, _) => {
      // request layout update
      if (pane.getScene != null) {
        javafx.application.Platform.runLater(() => {
          pane.getScene.getWindow.sizeToScene()
        })
      }
    })

    pane
  }

  /** creates a TitledPane from a ParameterSlider */
  def createParameterControl[T](paramSlider: ParameterSlider[T]): TitledPane = {
    val config = paramSlider.toSliderConfig
    val updateAction = paramSlider.updateAction(paramSlider.initialValue)
    createParameterControl(paramSlider.title, config, updateAction)
  }
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
    val pane = new TitledPane {
      text = title
      content = new VBox(5) {
        children = Seq(label, slider)
        padding = Insets(5)
      }
      expanded = false
      animated = false
    }

    // Force layout update when expanded/collapsed
    pane.expandedProperty().addListener((_, _, _) => {
      // Request layout update
      if (pane.getScene != null) {
        javafx.application.Platform.runLater(() => {
          pane.getScene.getWindow.sizeToScene()
        })
      }
    })

    pane
  }

  /** helper to create slider and pane */
  def createParameterControl(title: String, config: SliderConfig, updateAction: Double => Unit): TitledPane = {
    val (slider, label) = createParameterSlider(config, updateAction)
    createParameterPane(title, slider, label)
  }
}
