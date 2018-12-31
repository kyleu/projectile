package com.kyleu.projectile.util.tracing

object TraceData {
  // When you see this referenced, we screwed up.
  val todo = new TraceData()

  // When you see this referenced, it's intentionally bypassing tracing.
  val noop = new TraceData()
}

class TraceData protected () {
  def isNoop: Boolean = true

  def tag(k: String, v: String): Unit = {}
  def annotate(v: String): Unit = {}

  def logClass(cls: Class[_]): Unit = {}
}
