package com.kyleu.projectile.models.database.query

import java.sql.{Connection, PreparedStatement, Types}

import com.kyleu.projectile.models.database.query.Query.RawQuery
import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.util.{Logging, NullUtils}

import scala.annotation.tailrec

trait Queryable extends Logging {
  @tailrec
  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def prepare(stmt: PreparedStatement, values: Seq[Any], index: Int = 1): Unit = {
    if (values.nonEmpty) {
      values.headOption.getOrElse(throw new IllegalStateException()) match {
        case v if NullUtils.isNull(v) => stmt.setNull(index, Types.NULL)

        case Some(x) => stmt.setObject(index, Conversions.convert(x.asInstanceOf[AnyRef]))
        case None => stmt.setNull(index, Types.NULL)

        case v => stmt.setObject(index, Conversions.convert(v.asInstanceOf[AnyRef]))
      }
      prepare(stmt, values.drop(1), index + 1)
    }
  }

  def apply[A](connection: Connection, query: RawQuery[A])(implicit td: TraceData): A = {
    log.debug(s"${query.sql} with ${query.values.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(query.sql)
    try {
      prepare(stmt, query.values)
      val results = stmt.executeQuery()
      try {
        query.handle(results)
      } finally {
        results.close()
      }
    } finally {
      stmt.close()
    }
  }

  def apply[A](query: RawQuery[A])(implicit td: TraceData): A

  def query[A](q: RawQuery[A])(implicit td: TraceData): A = apply(q)
}
