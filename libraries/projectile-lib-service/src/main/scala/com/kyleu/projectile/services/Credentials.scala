package com.kyleu.projectile.services

object Credentials {
  val system = new Credentials {}
  val noop = new Credentials {}
}

trait Credentials
