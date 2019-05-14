package com.kyleu.projectile.util

object ExceptionUtils {
  def print(t: Throwable) = {
    val sw = new java.io.StringWriter
    t.printStackTrace(new java.io.PrintWriter(sw))
    sw.toString
  }
}
