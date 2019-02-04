package com.kyleu.projectile.services.form

import com.kyleu.projectile.models.entrypoint.Entrypoint
import com.kyleu.projectile.models.form.{FieldDefault, FieldHelper}
import org.scalajs.dom
import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}
import com.kyleu.projectile.util.Logging

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("FormService")
class FormService(id: String) extends Entrypoint("form") {
  val formEl = $("#" + id)
  if (formEl.length != 1) {
    throw new IllegalStateException(s"Found [${formEl.length}] form elements with id [$id]")
  }

  val fields = $(".data-input", formEl)
  fields.each { e: dom.Element => wireField($(e)) }

  val dates = $(".datepicker")
  if (dates.length > 0) {
    dates.each(e => {
      $(e).asInstanceOf[js.Dynamic].datepicker(js.Dynamic.literal(
        selectMonths = true,
        selectYears = 100,
        today = "Today",
        clear = "Clear",
        close = "Ok",
        closeOnSelect = false,
        format = "yyyy-mm-dd"
      ))
    })
  }
  val times = $(".timepicker")
  if (times.length > 0) {
    times.each(e => {
      $(e).asInstanceOf[js.Dynamic].timepicker(js.Dynamic.literal(
        default = "now",
        format = "HH:i",
        twelvehour = true,
        donetext = "OK",
        cleartext = "Clear",
        canceltext = "Cancel",
        autoclose = false,
        ampmclickable = true
      ))
    })
  }

  scalajs.js.Dynamic.global.$("select").formSelect()

  Logging.info(s"Form service started. [${fields.length}] fields")

  private[this] def wireField(checkbox: JQuery) = {
    val name = checkbox.data("name").toString
    val t = checkbox.data("type").toString
    Logging.info(s" - Wiring [$name:$t].")
    t match {
      case "boolean" => FieldHelper.onBoolean(name, formEl, checkbox)
      case "date" =>
        FieldHelper.onDate(name, formEl, checkbox)
        FieldDefault.onDefault(t, name, formEl, checkbox)
      case "time" =>
        FieldHelper.onTime(name, formEl, checkbox)
        FieldDefault.onDefault(t, name, formEl, checkbox)
      case "timestamp" =>
        FieldHelper.onDate(name + "-date", formEl, checkbox)
        FieldDefault.onDefault(t, name + "-date", formEl, checkbox)
        FieldHelper.onTime(name + "-time", formEl, checkbox)
        FieldDefault.onDefault(t, name + "-time", formEl, checkbox)
      case _ => FieldDefault.onDefault(t, name, formEl, checkbox)
    }
    name -> t
  }
}
