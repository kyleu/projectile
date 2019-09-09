// scalastyle:off file.size.limit
package com.kyleu.projectile.services.audit

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
  def getByPrimaryKey(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(AuditRecordQueries.getByPrimaryKey(id))(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID)(implicit trace: TraceData) = getByPrimaryKey(creds, id).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load auditRecordRow with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(AuditRecordQueries.getByPrimaryKeySeq(idSeq))(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(AuditRecordQueries.countAll(filters))(td))
  }
  override def getAll(
    creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(AuditRecordQueries.getAll(filters, orderBys, limit, offset))(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(AuditRecordQueries.searchCount(q, filters))(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(AuditRecordQueries.search(q, filters, orderBys, limit, offset))(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(AuditRecordQueries.searchExact(q, orderBys, limit, offset))(td))
  }

  def countByAuditId(creds: Credentials, auditId: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.auditId") { td =>
      db.queryF(AuditRecordQueries.CountByAuditId(auditId))(td)
    }
  }
  def getByAuditId(
    creds: Credentials, auditId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.auditId") { td =>
      db.queryF(AuditRecordQueries.GetByAuditId(auditId, orderBys, limit, offset))(td)
    }
  }
  def getByAuditIdSeq(creds: Credentials, auditIdSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (auditIdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.auditId.seq") { td =>
        db.queryF(AuditRecordQueries.GetByAuditIdSeq(auditIdSeq))(td)
      }
    }
  }

  def countById(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id") { td =>
      db.queryF(AuditRecordQueries.CountById(id))(td)
    }
  }
  def getById(
    creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id") { td =>
      db.queryF(AuditRecordQueries.GetById(id, orderBys, limit, offset))(td)
    }
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(AuditRecordQueries.GetByIdSeq(idSeq))(td)
      }
    }
  }

  def countByPk(creds: Credentials, pk: List[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.pk") { td =>
      db.queryF(AuditRecordQueries.CountByPk(pk))(td)
    }
  }
  def getByPk(
    creds: Credentials, pk: List[String], orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.pk") { td =>
      db.queryF(AuditRecordQueries.GetByPk(pk, orderBys, limit, offset))(td)
    }
  }
  def getByPkSeq(creds: Credentials, pkSeq: Seq[List[String]])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (pkSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.pk.seq") { td =>
        db.queryF(AuditRecordQueries.GetByPkSeq(pkSeq))(td)
      }
    }
  }

  def countByT(creds: Credentials, t: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.t") { td =>
      db.queryF(AuditRecordQueries.CountByT(t))(td)
    }
  }
  def getByT(
    creds: Credentials, t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.t") { td =>
      db.queryF(AuditRecordQueries.GetByT(t, orderBys, limit, offset))(td)
    }
  }
  def getByTSeq(creds: Credentials, tSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (tSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.t.seq") { td =>
        db.queryF(AuditRecordQueries.GetByTSeq(tSeq))(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: AuditRecord)(implicit trace: TraceData) = {
    traceF("insert") { td =>
      db.executeF(AuditRecordQueries.insert(model))(td).flatMap {
        case 1 => getByPrimaryKey(creds, model.id)(td)
        case _ => throw new IllegalStateException("Unable to find newly-inserted Audit Record")
      }
    }
  }
  def insertBatch(creds: Credentials, models: Seq[AuditRecord])(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(AuditRecordQueries.insertBatch(models))(td))
  }
  def create(creds: Credentials, fields: Seq[DataField])(implicit trace: TraceData) = {
    traceF("create") { td =>
      db.executeF(AuditRecordQueries.create(fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")))
      }
    }
  }

  def remove(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) =>
        db.executeF(AuditRecordQueries.removeByPrimaryKey(id))(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find AuditRecord matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField])(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Audit Record [$id]")
      case Some(_) => db.executeF(AuditRecordQueries.update(id, fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, id)(td).map {
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
