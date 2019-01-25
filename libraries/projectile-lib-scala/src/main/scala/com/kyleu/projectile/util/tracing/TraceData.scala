package com.kyleu.projectile.util.tracing

/** Use `noop` when you don't have access to an implicit TraceData */
object TraceData {
  // When you see this referenced, we screwed up.
  val todo = new TraceData()

  // When you see this referenced, it's intentionally bypassing tracing.
  val noop = new TraceData()
}

/** Used everywhere, `TraceData` is a common trait that exposes authentication and tracing information for OpenTracing services */
class TraceData protected () {
  def isNoop: Boolean = true

  def tag(k: String, v: String): Unit = {}
  def annotate(v: String): Unit = {}

  def logClass(cls: Class[_]): Unit = {}

  def traceId: String = "NOOP"
  def spanId: String = "NOOP"
}
