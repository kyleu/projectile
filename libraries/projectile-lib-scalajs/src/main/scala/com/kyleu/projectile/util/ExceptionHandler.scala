package com.kyleu.projectile.util

import org.scalajs.dom

import scala.util.control.NonFatal

object ExceptionHandler {
  private[this] var handlers = Seq.empty[(String, (Int, Int)) => Unit]

  def install() = dom.window.addEventListener("error", (e: dom.ErrorEvent) => {
    try {
      onError(e.message, e.filename, e.lineno, e.colno)
    } catch {
      case NonFatal(x) => Logging.error(s"Exception [${x.getClass.getSimpleName}: ${x.getMessage}] thrown in exception handler; this is madness!")
    }
    e.preventDefault
    true
  })

  def addHandler(f: (String, (Int, Int)) => Unit) = handlers = handlers :+ f

  private[this] def onError(msg: String, url: String, line: Int, col: Int) = {
    Logging.error(s"Unhandled error [$msg] at line [$line:$col] of url [$url]")
    handlers.foreach(_(msg, line -> col))
    // util.Logging.logJs(e)
  }
}
