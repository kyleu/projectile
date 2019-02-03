package com.kyleu.projectile.services.socket

import org.scalajs.dom.raw.Event

trait EventHandler[T] {
  def onConnect(): Unit

  def onMessage(msg: T): Unit

  def onError(err: Event): Unit
  def onClose(): Unit
}
