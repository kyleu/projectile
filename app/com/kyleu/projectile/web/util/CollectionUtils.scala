package com.kyleu.projectile.web.util

import java.util.concurrent.atomic.AtomicInteger

import scala.util.control.NonFatal

object CollectionUtils {
  def parLoop[T, U](
    seq: Seq[T],
    f: T => U,
    status: (Int, Set[T]) => Unit,
    errorHandler: Option[(T, Throwable) => U] = None,
    includeProgress: Boolean = false
  ) = {
    val counter = new AtomicInteger(0)

    def process(x: T) = try {
      f(x)
    } catch {
      case NonFatal(ex) =>
        ex.printStackTrace()
        errorHandler.map(_(x, ex)).getOrElse(throw ex)
    }

    if (includeProgress) {
      val inProgress = {
        import scala.collection.JavaConverters._
        java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap[T, java.lang.Boolean]).asScala
      }
      seq.par.map { x =>
        inProgress += x
        val ret = process(x)
        inProgress -= x
        status(counter.incrementAndGet(), inProgress.toSet)
        ret
      }.seq
    } else {
      seq.par.map { x =>
        val ret = process(x)
        status(counter.incrementAndGet(), Set.empty)
        ret
      }.seq
    }
  }
}
