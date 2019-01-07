package com.kyleu.projectile.services.database.schema

import java.sql.Connection

object MetadataTimezone {
  /*
  private[this] val q = new Query[Double] {
    override val sql = "select extract(timezone from now()) / 3600.0 as tz"
    override def reduce(rows: Iterator[JdbcRow]) = rows.next.as[Any]("tz") match {
      case d: Double => d
      case bd: java.math.BigDecimal => bd.doubleValue
      case s: String => s.replaceAllLiterally(":", ".").toDouble
      case x => throw new IllegalStateException(s"Encountered unexpected [${x.getClass.getSimpleName}:$x] from timezone query.")
    }
  }
  */

  def getTimezone(conn: Connection): Double = {
    //conn.query(q)
    0.0 // TODO
  }
}
