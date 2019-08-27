package com.kyleu.projectile.util

object Credentials {
  val anonymous = new Credentials {
    override def id = "anonymous"
    override def name = "anonymous"
    override def role = "user"
  }
  val system = new Credentials {
    override def id = "system"
    override def name = "system"
    override def role = "admin"
  }
  val noop = new Credentials {
    override def id = "noop"
    override def name = "noop"
    override def role = "admin"
  }
}

trait Credentials {
  def id: String
  def name: String
  def role: String
}
