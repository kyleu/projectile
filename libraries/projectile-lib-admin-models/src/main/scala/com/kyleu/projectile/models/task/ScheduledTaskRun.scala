package com.kyleu.projectile.models.task

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.Json

object ScheduledTaskRun {
  implicit val jsonEncoder: Encoder[ScheduledTaskRun] = (r: ScheduledTaskRun) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("task", r.task.asJson),
    ("arguments", r.arguments.asJson),
    ("status", r.status.asJson),
    ("output", r.output.asJson),
    ("started", r.started.asJson),
    ("completed", r.completed.asJson)
  )

  implicit val jsonDecoder: Decoder[ScheduledTaskRun] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[UUID]
    task <- c.downField("task").as[String]
    arguments <- c.downField("arguments").as[List[String]]
    status <- c.downField("status").as[String]
    output <- c.downField("output").as[Json]
    started <- c.downField("started").as[LocalDateTime]
    completed <- c.downField("completed").as[Option[LocalDateTime]]
  } yield ScheduledTaskRun(id, task, arguments, status, output, started, completed)

  def empty(
    id: UUID = UUID.randomUUID,
    task: String = "",
    arguments: List[String] = Nil,
    status: String = "",
    output: Json = Json.obj(),
    started: LocalDateTime = DateUtils.now,
    completed: Option[LocalDateTime] = None
  ) = {
    ScheduledTaskRun(id, task, arguments, status, output, started, completed)
  }
}

final case class ScheduledTaskRun(
    id: UUID,
    task: String,
    arguments: List[String],
    status: String,
    output: Json,
    started: LocalDateTime,
    completed: Option[LocalDateTime]
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("task", Some(task)),
    DataField("arguments", Some("{ " + arguments.mkString(", ") + " }")),
    DataField("status", Some(status)),
    DataField("output", Some(output.toString)),
    DataField("started", Some(started.toString)),
    DataField("completed", completed.map(_.toString))
  )

  lazy val taskOutput = extract[ScheduledTaskOutput](output)

  def toSummary = DataSummary(
    model = "scheduledTaskRunRow",
    pk = id.toString,
    entries = Map(
      "Task" -> Some(task),
      "Arguments" -> Some(arguments.mkString(", ")),
      "Status" -> Some(status),
      "Started" -> Some(started.toString),
      "Completed" -> completed.map(_.toString)
    )
  )
}
