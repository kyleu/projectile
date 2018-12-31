package com.kyleu.projectile.util

import org.slf4j.LoggerFactory

trait Logging {
  protected[this] lazy val log = LoggerFactory.getLogger(this.getClass)
}
