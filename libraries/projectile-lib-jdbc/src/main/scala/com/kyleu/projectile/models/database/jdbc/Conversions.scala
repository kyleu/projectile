package com.kyleu.projectile.models.database.jdbc

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import com.kyleu.projectile.util.DateUtils

object Conversions {
  @SuppressWarnings(Array("MethodReturningAny"))
  def convert(x: AnyRef): AnyRef = x match {
    case num: BigDecimal => num.underlying()
    case num: BigInt => BigDecimal(num).underlying()

    // Convert Joda times to UTC.
    case d: LocalDate => new java.sql.Date(DateUtils.toMillis(d.atStartOfDay))
    case dt: LocalDateTime => new java.sql.Timestamp(DateUtils.toMillis(dt))
    case zdt: ZonedDateTime => new java.sql.Timestamp(DateUtils.toMillisZoned(zdt))

    // case s: Seq[_] => "{" + s.map("\"" + _ + "\"").mkString(", ") + "}"

    // Pass everything else through.
    case _ => x
  }
}
