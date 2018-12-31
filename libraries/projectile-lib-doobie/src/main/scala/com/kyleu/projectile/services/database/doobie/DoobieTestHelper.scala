package com.kyleu.projectile.services.database.doobie

import com.kyleu.projectile.services.database.ApplicationDatabase
import com.kyleu.projectile.util.tracing.TracingService
import com.typesafe.config.ConfigFactory

object DoobieTestHelper {
  val cnf = ConfigFactory.load("application.test.conf")

  ApplicationDatabase.open(cfg = cnf, svc = TracingService.noop)

  val yolo = ApplicationDoobie.doobie.db.yolo
}
