// scalastyle:off file.size.limit
package com.kyleu.projectile.services.audit

import java.sql.Connection
import java.util.UUID

import com.google.inject.name.Named
import com.kyleu.projectile.models.audit.AuditRecord
import com.kyleu.projectile.models.queries.audit.AuditRecordQueries
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class AuditRecordService @javax.inject.Inject() (
    @Named("system") val db: JdbcDatabase,
    override val tracing: TracingService
)(implicit ec: ExecutionContext) extends ModelServiceHelper[AuditRecord]("auditRecordRow", "models" -> "AuditRecord") {
  def getByPrimaryKey(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(AuditRecordQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    getByPrimaryKey(creds, id, conn).map { opt =>
      opt.getOrElse(throw new IllegalStateException(s"Cannot load auditRecordRow with id [$id]"))
    }
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(AuditRecordQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(AuditRecordQueries.countAll(filters), conn)(td))
  }
  override def getAll(
    creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(AuditRecordQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(AuditRecordQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(AuditRecordQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(AuditRecordQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countByAuditId(creds: Credentials, auditId: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.auditId") { td =>
      db.queryF(AuditRecordQueries.CountByAuditId(auditId), conn)(td)
    }
  }
  def getByAuditId(
    creds: Credentials, auditId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.auditId") { td =>
      db.queryF(AuditRecordQueries.GetByAuditId(auditId, orderBys, limit, offset), conn)(td)
    }
  }
  def getByAuditIdSeq(creds: Credentials, auditIdSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (auditIdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.auditId.seq") { td =>
        db.queryF(AuditRecordQueries.GetByAuditIdSeq(auditIdSeq), conn)(td)
      }
    }
  }

  def countById(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id") { td =>
      db.queryF(AuditRecordQueries.CountById(id), conn)(td)
    }
  }
  def getById(
    creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id") { td =>
      db.queryF(AuditRecordQueries.GetById(id, orderBys, limit, offset), conn)(td)
    }
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(AuditRecordQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByPk(creds: Credentials, pk: List[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.pk") { td =>
      db.queryF(AuditRecordQueries.CountByPk(pk), conn)(td)
    }
  }
  def getByPk(
    creds: Credentials, pk: List[String], orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.pk") { td =>
      db.queryF(AuditRecordQueries.GetByPk(pk, orderBys, limit, offset), conn)(td)
    }
  }
  def getByPkSeq(creds: Credentials, pkSeq: Seq[List[String]], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (pkSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.pk.seq") { td =>
        db.queryF(AuditRecordQueries.GetByPkSeq(pkSeq), conn)(td)
      }
    }
  }

  def countByT(creds: Credentials, t: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.t") { td =>
      db.queryF(AuditRecordQueries.CountByT(t), conn)(td)
    }
  }
  def getByT(
    creds: Credentials, t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.t") { td =>
      db.queryF(AuditRecordQueries.GetByT(t, orderBys, limit, offset), conn)(td)
    }
  }
  def getByTSeq(creds: Credentials, tSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (tSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.t.seq") { td =>
        db.queryF(AuditRecordQueries.GetByTSeq(tSeq), conn)(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: AuditRecord, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("insert") { td =>
      db.executeF(AuditRecordQueries.insert(model), conn)(td).flatMap {
        case 1 => getByPrimaryKey(creds, model.id, conn)(td)
        case _ => throw new IllegalStateException("Unable to find newly-inserted Audit Record")
      }
    }
  }
  def insertBatch(creds: Credentials, models: Seq[AuditRecord], conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(AuditRecordQueries.insertBatch(models), conn)(td))
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("create") { td =>
      db.executeF(AuditRecordQueries.create(fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")), conn)
      }
    }
  }

  def remove(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        db.executeF(AuditRecordQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find AuditRecord matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Audit Record [$id]")
      case Some(_) => db.executeF(AuditRecordQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, id, conn)(td).map {
          case Some(newModel) =>
            newModel -> s"Updated [${fields.size}] fields of Audit Record [$id]"
          case None => throw new IllegalStateException(s"Cannot find AuditRecord matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find AuditRecord matching [$id]")
    })
  }

  def csvFor(totalCount: Int, rows: Seq[AuditRecord])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, AuditRecordQueries.fields)(td))
  }
}
