package com.kyleu.projectile.util

import scala.io.Source

/// Exposes a method to split a string to an ordered sequence of lines
object StringUtils {
  def lines(s: String) = Source.fromString(s).getLines.toIndexedSeq
}
