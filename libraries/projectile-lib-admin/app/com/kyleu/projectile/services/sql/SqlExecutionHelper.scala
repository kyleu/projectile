package com.kyleu.projectile.services.sql

import java.io.ByteArrayOutputStream
import java.sql.{Connection, PreparedStatement, Types}
import java.util.UUID

import com.github.tototoshi.csv.CSVWriter
import com.kyleu.projectile.models.database.Query
import com.kyleu.projectile.models.database.jdbc.Conversions
import com.kyleu.projectile.util.{Logging, NullUtils}
import com.kyleu.projectile.util.tracing.TraceData

import scala.annotation.tailrec
import scala.util.control.NonFatal

object SqlExecutionHelper extends Logging {
  def executeUnknown[A](connection: Connection, query: Query[A], resultId: Option[UUID])(implicit td: TraceData): Either[(Seq[String], A), Int] = {
    val q = new com.kyleu.projectile.models.database.jdbc.Queryable {}
    val actualValues = q.valsForJdbc(connection, query.values)
    log.debug(s"${query.sql} with ${actualValues.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(query.sql)
    try {
      try {
        prepare(stmt, actualValues)
      } catch {
        case NonFatal(x) =>
          log.error(s"Unable to prepare raw query [${query.sql}]", x)
          throw x
      }
      val isResultset = stmt.execute()
      if (isResultset) {
        val res = stmt.getResultSet
        try {
          val md = res.getMetaData
          val columns = (1 to md.getColumnCount).map(i => md.getColumnName(i))
          Left(columns -> query.handle(res))
        } catch {
          case NonFatal(x) =>
            log.error(s"Unable to handle query results for [${query.sql}]", x)
            throw x
        } finally {
          res.close()
        }
      } else {
        Right(stmt.getUpdateCount)
      }
    } finally {
      stmt.close()
    }
  }

  def csvFor(records: Seq[Seq[Option[String]]], fields: Seq[String]) = {
    val os = new ByteArrayOutputStream()
    val writer = CSVWriter.open(os)
    writer.writeRow(fields)
    records.foreach(r => writer.writeRow(r.map {
      case Some(o) => o
      case None => NullUtils.inst
    }))
    new String(os.toByteArray)
  }

  @tailrec
  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def prepare(stmt: PreparedStatement, values: Seq[Any], index: Int = 1): Unit = if (values.nonEmpty) {
    values.headOption.getOrElse(throw new IllegalStateException()) match {
      case v if NullUtils.isNull(v) => stmt.setNull(index, Types.NULL)

      case Some(x) => stmt.setObject(index, Conversions.convert(x.asInstanceOf[AnyRef]))
      case None => stmt.setNull(index, Types.NULL)

      case v => stmt.setObject(index, Conversions.convert(v.asInstanceOf[AnyRef]))
    }
    prepare(stmt, values.drop(1), index + 1)
  }
}
