package com.kyleu.projectile.util

import scala.scalajs.js

@scala.scalajs.js.annotation.JSExportTopLevel("ClipboardUtils")
object ClipboardUtils {
  @scala.scalajs.js.annotation.JSExport
  def writeClipboard(s: String) = org.scalajs.dom.window.navigator.asInstanceOf[js.Dynamic].clipboard.writeText(s)
}
