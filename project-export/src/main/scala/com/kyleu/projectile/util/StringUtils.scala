package com.kyleu.projectile.util

import scala.io.Source

object StringUtils {
  def lines(s: String) = Source.fromString(s).getLines.toIndexedSeq
}
