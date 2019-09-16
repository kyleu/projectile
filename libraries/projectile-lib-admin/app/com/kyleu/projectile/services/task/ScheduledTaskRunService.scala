// scalastyle:off file.size.limit
package com.kyleu.projectile.services.task

import java.sql.Connection

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.services.audit.AuditHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.queries.task.ScheduledTaskRunQueries
import com.kyleu.projectile.models.task.ScheduledTaskRun
import javax.inject.Named

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ScheduledTaskRunService @javax.inject.Inject() (
    override val tracing: TracingService, @Named("system") db: JdbcDatabase
)(implicit ec: ExecutionContext) extends ModelServiceHelper[ScheduledTaskRun]("scheduledTaskRun", "tools" -> "ScheduledTaskRun") {
  def getByPrimaryKey(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(ScheduledTaskRunQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    getByPrimaryKey(creds, id, conn).map { opt =>
      opt.getOrElse(throw new IllegalStateException(s"Cannot load scheduledTaskRun with id [$id]"))
    }
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(ScheduledTaskRunQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(ScheduledTaskRunQueries.countAll(filters), conn)(td))
  }
  override def getAll(
    creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(ScheduledTaskRunQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(ScheduledTaskRunQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(ScheduledTaskRunQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(ScheduledTaskRunQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countByArguments(creds: Credentials, arguments: List[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.arguments") { td =>
      db.queryF(ScheduledTaskRunQueries.CountByArguments(arguments), conn)(td)
    }
  }
  def getByArguments(
    creds: Credentials, arguments: List[String], orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.arguments") { td =>
      db.queryF(ScheduledTaskRunQueries.GetByArguments(arguments, orderBys, limit, offset), conn)(td)
    }
  }
  def getByArgumentsSeq(creds: Credentials, argumentsSeq: Seq[List[String]], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (argumentsSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.arguments.seq") { td =>
        db.queryF(ScheduledTaskRunQueries.GetByArgumentsSeq(argumentsSeq), conn)(td)
      }
    }
  }

  def countById(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id") { td =>
      db.queryF(ScheduledTaskRunQueries.CountById(id), conn)(td)
    }
  }
  def getById(
    creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id") { td =>
      db.queryF(ScheduledTaskRunQueries.GetById(id, orderBys, limit, offset), conn)(td)
    }
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(ScheduledTaskRunQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByStarted(creds: Credentials, started: LocalDateTime, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.started") { td =>
      db.queryF(ScheduledTaskRunQueries.CountByStarted(started), conn)(td)
    }
  }
  def getByStarted(
    creds: Credentials, started: LocalDateTime, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.started") { td =>
      db.queryF(ScheduledTaskRunQueries.GetByStarted(started, orderBys, limit, offset), conn)(td)
    }
  }
  def getByStartedSeq(creds: Credentials, startedSeq: Seq[LocalDateTime], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (startedSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.started.seq") { td =>
        db.queryF(ScheduledTaskRunQueries.GetByStartedSeq(startedSeq), conn)(td)
      }
    }
  }

  def countByStatus(creds: Credentials, status: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.status") { td =>
      db.queryF(ScheduledTaskRunQueries.CountByStatus(status), conn)(td)
    }
  }
  def getByStatus(
    creds: Credentials, status: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.status") { td =>
      db.queryF(ScheduledTaskRunQueries.GetByStatus(status, orderBys, limit, offset), conn)(td)
    }
  }
  def getByStatusSeq(creds: Credentials, statusSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (statusSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.status.seq") { td =>
        db.queryF(ScheduledTaskRunQueries.GetByStatusSeq(statusSeq), conn)(td)
      }
    }
  }

  def countByTask(creds: Credentials, task: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.task") { td =>
      db.queryF(ScheduledTaskRunQueries.CountByTask(task), conn)(td)
    }
  }
  def getByTask(
    creds: Credentials, task: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.task") { td =>
      db.queryF(ScheduledTaskRunQueries.GetByTask(task, orderBys, limit, offset), conn)(td)
    }
  }
  def getByTaskSeq(creds: Credentials, taskSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (taskSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.task.seq") { td =>
        db.queryF(ScheduledTaskRunQueries.GetByTaskSeq(taskSeq), conn)(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: ScheduledTaskRun, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("insert") { td =>
      db.executeF(ScheduledTaskRunQueries.insert(model), conn)(td).flatMap {
        case 1 => getByPrimaryKey(creds, model.id, conn)(td).map(_.map { n =>
          AuditHelper.onInsert("ScheduledTaskRun", Seq(n.id.toString), n.toDataFields, creds)
          n
        })
        case _ => throw new IllegalStateException("Unable to find newly-inserted Scheduled Task Run")
      }
    }
  }
  def insertBatch(creds: Credentials, models: Seq[ScheduledTaskRun], conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(ScheduledTaskRunQueries.insertBatch(models), conn)(td))
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("create") { td =>
      db.executeF(ScheduledTaskRunQueries.create(fields), conn)(td).flatMap { _ =>
        AuditHelper.onInsert("ScheduledTaskRun", Seq(fieldVal(fields, "id")), fields, creds)
        getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")), conn)(td)
      }
    }
  }

  def remove(creds: Credentials, id: UUID)(implicit trace: TraceData, conn: Option[Connection] = None) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        AuditHelper.onRemove("ScheduledTaskRun", Seq(id.toString), current.toDataFields, creds)
        db.executeF(ScheduledTaskRunQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find ScheduledTaskRun matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Scheduled Task Run [$id]")
      case Some(current) => db.executeF(ScheduledTaskRunQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, fields.find(_.k == "id").flatMap(_.v).map(s => UUID.fromString(s)).getOrElse(id), conn)(td).map {
          case Some(newModel) =>
            AuditHelper.onUpdate("ScheduledTaskRun", Seq(id.toString), current.toDataFields, fields, creds)
            newModel -> s"Updated [${fields.size}] fields of Scheduled Task Run [$id]"
          case None => throw new IllegalStateException(s"Cannot find ScheduledTaskRun matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find ScheduledTaskRun matching [$id]")
    })
  }

  def csvFor(totalCount: Int, s: Seq[ScheduledTaskRun])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, s, ScheduledTaskRunQueries.fields)(td))
  }
}
