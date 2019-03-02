package com.kyleu.projectile.services.form

import com.kyleu.projectile.util.StringUtils
import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery => $}

import scala.scalajs.js

object FormHelper {
  def process() = {
    processDates()
    processTags()
  }

  def tagEditor(q: JQuery) = {
    val t = q.data("t").toString
    val initialValues = StringUtils.toList(q.value().toString)
    val key = q.attr("id").get.stripPrefix("input-")
    val checkbox = $(s"#$key-include")
    q.asInstanceOf[js.Dynamic].tagEditor(js.Dynamic.literal(
      forceLowercase = false,
      removeDuplicates = t == "set" || t == "map",
      initialTags = js.Array(initialValues: _*),
      onChange = () => {
        val n = StringUtils.toList(q.value().toString)
        checkbox.prop("checked", n != initialValues)
      }
    ))
  }

  def dateEditor(dates: JQuery) = dates.asInstanceOf[js.Dynamic].datepicker(js.Dynamic.literal(
    selectMonths = true,
    selectYears = 100,
    today = "Today",
    clear = "Clear",
    close = "Ok",
    closeOnSelect = false,
    format = "yyyy-mm-dd"
  ))

  private[this] def processDates() = {
    val dates = $(".datepicker")
    if (dates.length > 0) {
      dateEditor(dates)
    }
  }

  private[this] def processTags() = {
    val tagEditors = $(".tag-editor")
    if (tagEditors.length > 0) {
      tagEditors.each((e: dom.Element) => tagEditor($(e)))
    }
  }
}
