package com.kyleu.projectile.util

import scala.scalajs.js

object JavaScriptUtils {
  private[this] val offset = new scalajs.js.Date().getTimezoneOffset().toLong

  def niceCurrentTime() = DateUtils.niceTime(DateUtils.now.toLocalTime.plusMinutes(offset))

  def as[T <: js.Any](x: Any): T = x.asInstanceOf[T]
  def asDynamic(o: js.Any) = o.asInstanceOf[js.Dynamic]

  def el(id: String) = {
    val ret = org.scalajs.jquery.jQuery("#" + id)
    if (ret.length != 1) { throw new IllegalStateException(s"Found [${ret.length}] elements with id [$id]") }
    ret
  }
}
