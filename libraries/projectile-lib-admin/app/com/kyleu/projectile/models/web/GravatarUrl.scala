package com.kyleu.projectile.models.web

object GravatarUrl {
  private[this] val md = java.security.MessageDigest.getInstance("MD5")

  def apply(email: String) = {
    val hash = md.digest(email.trim.toLowerCase.getBytes("CP1252")).map("%02X".format(_)).mkString.toLowerCase
    s"https://www.gravatar.com/avatar/$hash?d=mp&r=x"
  }
}
