package com.kyleu.projectile.models.reporting

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import play.api.mvc.Call

object ReportResult {
  implicit val jsonEncoder: Encoder[ReportResult] = (r: ReportResult) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("report", r.report.asJson),
    ("args", r.args.asJson),
    ("columns", r.columns.map(c => Seq(c._1, c._2)).asJson),
    ("rows", r.rows.map(_.map(_.map(_._1.toString))).asJson),
    ("errors", r.errors.map(e => e._1 + ": " + e._2).asJson),
    ("runBy", r.runBy.asJson),
    ("durationMs", r.durationMs.asJson),
    ("occurred", r.occurred.asJson)
  )
}

final case class ReportResult(
    id: UUID = UUID.randomUUID,
    report: ProjectileReport,
    args: Map[String, String],
    columns: Seq[(String, String)],
    rows: Seq[Seq[Option[(Any, Option[Call])]]],
    errors: Seq[(String, String)] = Nil,
    runBy: UUID,
    durationMs: Int,
    occurred: LocalDateTime = DateUtils.now
) {
  def sortBy(sort: Option[String]) = sort match {
    case None => this
    case Some(sortCol) =>
      val colIdx = columns.indexWhere(_._1 == sortCol.stripPrefix("-"))
      val newRows = rows.sortBy {
        case row if row.size > colIdx => row(colIdx).map(_._1.toString)
        case _ => None
      }
      this.copy(rows = if (sortCol.startsWith("-")) { newRows.reverse } else { newRows })
  }
}
