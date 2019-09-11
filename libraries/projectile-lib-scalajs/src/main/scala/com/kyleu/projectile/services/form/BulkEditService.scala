package com.kyleu.projectile.services.form

import com.kyleu.projectile.util.Logging
import org.scalajs.jquery.{JQueryEventObject, jQuery => $}

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("BulkEditService")
class BulkEditService(id: String) {
  val form = $("#" + id)
  if (form.length != 1) {
    throw new IllegalStateException(s"Found [${form.length}] form elements with id [$id]")
  }
  val hiddenEl = $(".primaryKeys", form)
  if (hiddenEl.length != 1) {
    throw new IllegalStateException(s"Found [${hiddenEl.length}] hidden form elements with id [primaryKey]")
  }

  private[this] var curr = getPks(hiddenEl.value().toString)
  Logging.info(s"Bulk edit service started with [${curr.length}] primary keys")

  val closeButtons = $(".remove-pk", form).click((ev: JQueryEventObject) => {
    val t = $(ev.delegateTarget)
    curr = curr.filterNot(_ == t.data("pk").toString)
    t.closest("tr").remove()
    setPks()
    false
  })

  private[this] def getPks(s: String) = {
    s.split("//").map(_.trim()).filter(_.nonEmpty).toList
  }

  private[this] def setPks() = hiddenEl.value(curr.mkString("//"))
}
