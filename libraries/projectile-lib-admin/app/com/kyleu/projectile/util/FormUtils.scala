package com.kyleu.projectile.util

object FormUtils {
  def inputClass(isPk: Boolean, isFk: Boolean) = if (isPk) { "primary-key" } else if (isFk) { "foreign-key" } else { "" }
}

