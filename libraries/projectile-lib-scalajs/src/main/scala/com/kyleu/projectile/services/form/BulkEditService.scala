package com.kyleu.projectile.services.form

import com.kyleu.projectile.util.Logging
import org.scalajs.jquery.{JQueryEventObject, jQuery => $}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("BulkEditService")
class BulkEditService(id: String, fieldCount: Int) {
  val tbody = $("#bulk-rows tbody")
  if (tbody.length != 1) {
    throw new IllegalStateException(s"Found [${tbody.length}] table elements with id [bulk-rows]")
  }
  val form = $("#" + id)
  if (form.length != 1) {
    throw new IllegalStateException(s"Found [${form.length}] form elements with id [$id]")
  }
  val hiddenEl = $(".primaryKeys", form)
  if (hiddenEl.length != 1) {
    throw new IllegalStateException(s"Found [${hiddenEl.length}] hidden form elements with id [primaryKey]")
  }

  private[this] var curr = getPks(hiddenEl.value().toString)
  Logging.info(s"Bulk edit service started with [${curr.length}] primary keys and [$fieldCount] fields")

  val closeButtons = $(".remove-pk", form).click((ev: JQueryEventObject) => {
    val t = $(ev.delegateTarget)
    curr = curr.filterNot(_ == t.data("pk").toString)
    t.closest("tr").remove()
    setPks()
    false
  })

  @JSExport
  def addPk(pkString: String) = {
    val extra = s"""<td colspan="${fieldCount - 1}"><em>Manually added</em></td>"""
    val icon = """<i class="material-icons">close</i>"""

    val pks = pkString.split(" ").flatMap(_.split(",")).map(_.trim).filter(_.nonEmpty)
    pks.foreach { pk =>
      val pkCol = s"<td>$pk</td>"
      val removeLink = s"""<td style="text-align: right;"><a class="remove-pk manual" data-pk="$pk" href="" title="Remove from editing">$icon</a></td>"""
      tbody.append(s"""<tr>$pkCol$extra$removeLink</tr>""")
      $(".remove-pk.manual", form).click((ev: JQueryEventObject) => {
        val t = $(ev.delegateTarget)
        curr = curr.filterNot(_ == t.data("pk").toString)
        t.closest("tr").remove()
        setPks()
        false
      })
      curr = curr :+ pk
    }
    setPks()
  }

  private[this] def getPks(s: String) = {
    s.split("//").map(_.trim()).filter(_.nonEmpty).toList
  }

  private[this] def setPks() = hiddenEl.value(curr.mkString("//"))
}
