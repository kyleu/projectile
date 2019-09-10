// scalastyle:off file.size.limit
package com.kyleu.projectile.services.audit

import java.util.UUID

import com.google.inject.name.Named
import com.kyleu.projectile.models.audit.{Audit, AuditRecord}
import com.kyleu.projectile.models.module.ApplicationFeature
import com.kyleu.projectile.models.queries.audit.{AuditQueries, AuditRecordQueries}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class AuditService @javax.inject.Inject() (
    @Named("system") val db: JdbcDatabase,
    override val tracing: TracingService
)(implicit ec: ExecutionContext) extends ModelServiceHelper[Audit]("audit", "models" -> "Audit") {
  def getByModel(creds: Credentials, model: String, pk: Any*)(implicit trace: TraceData) = {
    if (ApplicationFeature.enabled(ApplicationFeature.Audit)) {
      db.queryF(AuditRecordQueries.GetByRelation(model, pk.map(_.toString).toList))
    } else {
      Future.successful(Nil)
    }
  }

  def callback(a: Audit, records: Seq[AuditRecord])(implicit trace: TraceData) = {
    if (records.exists(r => r.changes.nonEmpty)) {
      persist(a, records)
      log.info(a.changeLog)
    } else {
      log.info(s"Ignoring audit [${a.id}], as it has no changes")
    }
  }

  protected[this] def persist(a: Audit, records: Seq[AuditRecord])(implicit trace: TraceData) = {
    log.debug(s"Persisting audit [${a.id}]...")
    val ret = db.executeF(AuditQueries.insert(a)).flatMap { _ =>
      db.executeF(AuditRecordQueries.insertBatch(records)).map { _ =>
        log.debug(s"Persisted audit [${a.id}] with [${records.size}] records")
      }
    }
    ret.failed.foreach(x => log.warn(s"Unable to persist audit [${a.id}].", x))
    ret
  }

  def getByPrimaryKey(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(AuditQueries.getByPrimaryKey(id))(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID)(implicit trace: TraceData) = getByPrimaryKey(creds, id).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load audit with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(AuditQueries.getByPrimaryKeySeq(idSeq))(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(AuditQueries.countAll(filters))(td))
  }
  override def getAll(
    creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(AuditQueries.getAll(filters, orderBys, limit, offset))(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(AuditQueries.searchCount(q, filters))(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(AuditQueries.search(q, filters, orderBys, limit, offset))(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(AuditQueries.searchExact(q, orderBys, limit, offset))(td))
  }

  def countByAct(creds: Credentials, act: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.act") { td =>
      db.queryF(AuditQueries.CountByAct(act))(td)
    }
  }
  def getByAct(
    creds: Credentials, act: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.act") { td =>
      db.queryF(AuditQueries.GetByAct(act, orderBys, limit, offset))(td)
    }
  }
  def getByActSeq(creds: Credentials, actSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (actSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.act.seq") { td =>
        db.queryF(AuditQueries.GetByActSeq(actSeq))(td)
      }
    }
  }

  def countByApp(creds: Credentials, app: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.app") { td =>
      db.queryF(AuditQueries.CountByApp(app))(td)
    }
  }
  def getByApp(
    creds: Credentials, app: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.app") { td =>
      db.queryF(AuditQueries.GetByApp(app, orderBys, limit, offset))(td)
    }
  }
  def getByAppSeq(creds: Credentials, appSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (appSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.app.seq") { td =>
        db.queryF(AuditQueries.GetByAppSeq(appSeq))(td)
      }
    }
  }

  def countByClient(creds: Credentials, client: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.client") { td =>
      db.queryF(AuditQueries.CountByClient(client))(td)
    }
  }
  def getByClient(
    creds: Credentials, client: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.client") { td =>
      db.queryF(AuditQueries.GetByClient(client, orderBys, limit, offset))(td)
    }
  }
  def getByClientSeq(creds: Credentials, clientSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (clientSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.client.seq") { td =>
        db.queryF(AuditQueries.GetByClientSeq(clientSeq))(td)
      }
    }
  }

  def countById(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id") { td =>
      db.queryF(AuditQueries.CountById(id))(td)
    }
  }
  def getById(
    creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id") { td =>
      db.queryF(AuditQueries.GetById(id, orderBys, limit, offset))(td)
    }
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(AuditQueries.GetByIdSeq(idSeq))(td)
      }
    }
  }

  def countByServer(creds: Credentials, server: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.server") { td =>
      db.queryF(AuditQueries.CountByServer(server))(td)
    }
  }
  def getByServer(
    creds: Credentials, server: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.server") { td =>
      db.queryF(AuditQueries.GetByServer(server, orderBys, limit, offset))(td)
    }
  }
  def getByServerSeq(creds: Credentials, serverSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (serverSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.server.seq") { td =>
        db.queryF(AuditQueries.GetByServerSeq(serverSeq))(td)
      }
    }
  }

  def countByUserId(creds: Credentials, userId: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.userId") { td =>
      db.queryF(AuditQueries.CountByUserId(userId))(td)
    }
  }
  def getByUserId(
    creds: Credentials, userId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.userId") { td =>
      db.queryF(AuditQueries.GetByUserId(userId, orderBys, limit, offset))(td)
    }
  }
  def getByUserIdSeq(creds: Credentials, userIdSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (userIdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.userId.seq") { td =>
        db.queryF(AuditQueries.GetByUserIdSeq(userIdSeq))(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: Audit)(implicit trace: TraceData) = {
    traceF("insert") { td =>
      db.executeF(AuditQueries.insert(model))(td).flatMap {
        case 1 => getByPrimaryKey(creds, model.id)(td)
        case _ => throw new IllegalStateException("Unable to find newly-inserted Audit")
      }
    }
  }
  def insertBatch(creds: Credentials, models: Seq[Audit])(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(AuditQueries.insertBatch(models))(td))
  }
  def create(creds: Credentials, fields: Seq[DataField])(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("create") { td =>
      db.executeF(AuditQueries.create(fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")))
      }
    }
  }

  def remove(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) =>
        db.executeF(AuditQueries.removeByPrimaryKey(id))(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find Audit matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField])(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Audit [$id]")
      case Some(_) => db.executeF(AuditQueries.update(id, fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, id)(td).map {
          case Some(newModel) =>
            newModel -> s"Updated [${fields.size}] fields of Audit [$id]"
          case None => throw new IllegalStateException(s"Cannot find Audit matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find Audit matching [$id]")
    })
  }

  def csvFor(totalCount: Int, rows: Seq[Audit])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, AuditQueries.fields)(td))
  }
}
