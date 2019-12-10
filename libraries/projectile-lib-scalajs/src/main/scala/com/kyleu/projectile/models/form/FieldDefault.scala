package com.kyleu.projectile.models.form

import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}
import com.kyleu.projectile.util.Logging

import scala.scalajs.js

object FieldDefault {
  def onDefault(t: String, name: String, formEl: JQuery, checkbox: JQuery) = {
    val input = $(s"#input-$name", formEl)
    if (input.length != 1) {
      throw new IllegalStateException(s"Found [${input.length}] $t input elements with id [input-$name]")
    }

    if (input.hasClass("nullable")) {
      val nullable = $(s"#nullable-$name", formEl)
      if (nullable.length != 1) {
        throw new IllegalStateException(s"Found [${nullable.length}] $t nullable elements with id [nullable-$name]")
      }
      var lastVal: Option[String] = None
      nullable.click { _: JQueryEventObject =>
        lastVal match {
          case Some(v) =>
            lastVal = None
            input.value(v)
          case None =>
            lastVal = Some(input.value().toString)
            input.value("∅")
        }
      }
    }

    if (input.hasClass("lookup")) {
      val model = input.data("model").toString
      val url = input.data("url").toString
      Logging.info(s" - Wiring [$model] autocomplete for [$name] as [$url]")

      val dyn = js.Dynamic.global.$(s"#input-$name", formEl)

      val options = js.Dynamic.literal(
        url = (s: String) => s"$url?q=$s",
        getValue = "pk",
        template = js.Dynamic.literal(
          `type` = "description",
          fields = js.Dynamic.literal(
            description = "title"
          )
        )
      )

      dyn.easyAutocomplete(options)
    }
    val originalValue = input.value().toString
    input.keyup((_: JQueryEventObject) => {
      checkbox.prop("checked", originalValue != input.value().toString)
    })
    input.on("change", (_: JQueryEventObject) => {
      checkbox.prop("checked", originalValue != input.value().toString)
    })
  }

  def onDatetimeDefault(t: String, name: String, formEl: JQuery, checkbox: JQuery) = {
    val dateInput = $(s"#input-$name-date", formEl)
    if (dateInput.length != 1) {
      throw new IllegalStateException(s"Found [${dateInput.length}] $t date input elements with id [input-$name]")
    }

    val timeInput = $(s"#input-$name-date", formEl)
    if (timeInput.length != 1) {
      throw new IllegalStateException(s"Found [${timeInput.length}] $t time input elements with id [input-$name]")
    }

    if (dateInput.hasClass("nullable")) {
      val nullable = $(s"#nullable-$name", formEl)
      if (nullable.length != 1) {
        throw new IllegalStateException(s"Found [${nullable.length}] $t nullable elements with id [nullable-$name]")
      }
      var lastDateVal: Option[String] = None
      var lastTimeVal: Option[String] = None
      nullable.click { _: JQueryEventObject =>
        lastDateVal match {
          case Some(v) =>
            lastDateVal = None
            dateInput.value(v)
          case None =>
            lastDateVal = Some(dateInput.value().toString)
            dateInput.value("∅")
        }
        lastTimeVal match {
          case Some(v) =>
            lastTimeVal = None
            timeInput.value(v)
          case None =>
            lastTimeVal = Some(timeInput.value().toString)
            timeInput.value("∅")
        }
        checkbox.prop("checked", true)
      }
    }
    val originalTimeValue = timeInput.value().toString
    timeInput.keyup((_: JQueryEventObject) => {
      checkbox.prop("checked", originalTimeValue != timeInput.value().toString)
    })
    timeInput.on("change", (_: JQueryEventObject) => {
      checkbox.prop("checked", originalTimeValue != timeInput.value().toString)
    })
    val originalDateValue = dateInput.value().toString
    dateInput.keyup((_: JQueryEventObject) => {
      checkbox.prop("checked", originalDateValue != dateInput.value().toString)
    })
    dateInput.on("change", (_: JQueryEventObject) => {
      checkbox.prop("checked", originalDateValue != dateInput.value().toString)
    })
  }
}
