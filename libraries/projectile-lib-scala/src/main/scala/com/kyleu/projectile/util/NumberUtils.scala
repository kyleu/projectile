package com.kyleu.projectile.util

/// Currently only provides a cross-platform method for formatting numbers
object NumberUtils {
  def withCommas(i: Int) = i.toString.reverse.grouped(3).mkString(",").reverse
  def withCommas(l: Long) = l.toString.reverse.grouped(3).mkString(",").reverse
}
