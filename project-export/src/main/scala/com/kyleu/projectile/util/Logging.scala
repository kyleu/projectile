package com.kyleu.projectile.util

import org.slf4j.LoggerFactory

trait Logging {
  protected[this] val log = LoggerFactory.getLogger(s"${Config.metricsId}.${this.getClass.getSimpleName.replace("$", "")}")
}
