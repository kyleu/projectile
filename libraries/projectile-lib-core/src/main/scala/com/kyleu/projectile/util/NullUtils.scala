package com.kyleu.projectile.util

/** Rather than use `null` references in your code, NullUtils is provided for common operations and values involving nulls */
object NullUtils {
  val char = '∅'
  val str = char.toString

  val inst = None.orNull

  def isNull(v: Any) = v == inst
  def notNull(v: Any) = v != inst
}
