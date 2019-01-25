package com.kyleu.projectile.util.metrics

import com.typesafe.config.Config

/** Case class containing all of the information needed to report metrics. Usually created from a config file */
@javax.inject.Singleton
class MetricsConfig @javax.inject.Inject() (cnf: Config) {
  val micrometerEnabled = cnf.getBoolean("metrics.micrometer.enabled")
  val micrometerEngine = cnf.getString("metrics.micrometer.engine")
  val micrometerHost = cnf.getString("metrics.micrometer.host")

  val tracingEnabled = cnf.getBoolean("metrics.tracing.enabled")
  val tracingServer = Option(System.getenv("ZIPKIN_SERVICE_HOST")) match {
    case Some(host) => host
    case _ => cnf.getString("metrics.tracing.server")
  }
  val tracingPort = Option(System.getenv("ZIPKIN_SERVICE_PORT")) match {
    case Some(port) => port.toInt
    case _ => cnf.getInt("metrics.tracing.port")
  }
  val tracingSampleRate = cnf.getDouble("metrics.tracing.sampleRate").toFloat
}

