package com.kyleu.projectile.services.database.doobie

import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.tracing.TracingService
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global

object DoobieTestHelper {
  val cnf = ConfigFactory.load("application.test.conf")

  val db = new JdbcDatabase("application", "database.application")
  db.open(cfg = cnf, tracing = TracingService.noop)

  val doob = new DoobieQueryService(db.source)

  val yolo = doob.db.yolo
}
