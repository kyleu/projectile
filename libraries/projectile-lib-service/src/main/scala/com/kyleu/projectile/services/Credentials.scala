package com.kyleu.projectile.services

object Credentials {
  val anonymous = new Credentials {
    override def id = "anonymous"
    override def name = "anonymous"
  }
  val system = new Credentials {
    override def id = "system"
    override def name = "system"
  }
  val noop = new Credentials {
    override def id = "noop"
    override def name = "noop"
  }
}

trait Credentials {
  def id: String
  def name: String
}
