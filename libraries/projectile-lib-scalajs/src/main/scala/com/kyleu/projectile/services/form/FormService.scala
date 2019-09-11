package com.kyleu.projectile.services.form

import com.kyleu.projectile.models.entrypoint.Entrypoint
import com.kyleu.projectile.models.form.{FieldDefault, FieldHelper}
import com.kyleu.projectile.util.Logging
import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery => $}

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
  FormHelper.process()

  scalajs.js.Dynamic.global.$("select").formSelect(js.Dynamic.literal(
    dropdownOptions = js.Dynamic.literal(container = dom.document.body)
  ))

  Logging.info(s"Form service started with [${fields.length}] fields")

  private[this] def wireField(checkbox: JQuery) = {
    val name = checkbox.data("name").toString
    val t = checkbox.data("type").toString
    Logging.info(s" - Wiring [$name:$t].")
    t match {
      case "boolean" => FieldHelper.onBoolean(name, formEl, checkbox)
      case "date" => FieldHelper.wire(name, formEl, checkbox)
      case "time" => FieldHelper.wire(name, formEl, checkbox)
      case "timestamp" => FieldHelper.wireTimestamp(name, formEl, checkbox)
      case _ => // noop
    }
    t match {
      case "boolean" => // noop
      case "timestamp" =>
        FieldDefault.onDefault(t, name + "-date", formEl, checkbox)
        FieldDefault.onDefault(t, name + "-time", formEl, checkbox)
      case _ => FieldDefault.onDefault(t, name, formEl, checkbox)
    }
    name -> t
  }
}
