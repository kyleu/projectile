/* Generated File */
package services.b

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.services.audit.AuditHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import java.sql.Connection
import java.util.UUID
import models.b.BottomRow
import models.queries.b.BottomRowQueries
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class BottomRowService @javax.inject.Inject() (val db: JdbcDatabase, override val tracing: TracingService)(implicit ec: ExecutionContext) extends ModelServiceHelper[BottomRow]("bottomRow", "b" -> "BottomRow") {
  def getByPrimaryKey(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(BottomRowQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = getByPrimaryKey(creds, id, conn).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load bottomRow with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(BottomRowQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(BottomRowQueries.countAll(filters), conn)(td))
  }
  override def getAll(creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(BottomRowQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(BottomRowQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(BottomRowQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(BottomRowQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countById(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id")(td => db.queryF(BottomRowQueries.CountById(id), conn)(td))
  }
  def getById(creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id")(td => db.queryF(BottomRowQueries.GetById(id, orderBys, limit, offset), conn)(td))
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(BottomRowQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByT(creds: Credentials, t: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.t")(td => db.queryF(BottomRowQueries.CountByT(t), conn)(td))
  }
  def getByT(creds: Credentials, t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.t")(td => db.queryF(BottomRowQueries.GetByT(t, orderBys, limit, offset), conn)(td))
  }
  def getByTSeq(creds: Credentials, tSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (tSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.t.seq") { td =>
        db.queryF(BottomRowQueries.GetByTSeq(tSeq), conn)(td)
      }
    }
  }

  def countByTopId(creds: Credentials, topId: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.topId")(td => db.queryF(BottomRowQueries.CountByTopId(topId), conn)(td))
  }
  def getByTopId(creds: Credentials, topId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.topId")(td => db.queryF(BottomRowQueries.GetByTopId(topId, orderBys, limit, offset), conn)(td))
  }
  def getByTopIdSeq(creds: Credentials, topIdSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (topIdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.topId.seq") { td =>
        db.queryF(BottomRowQueries.GetByTopIdSeq(topIdSeq), conn)(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: BottomRow, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insert")(td => db.executeF(BottomRowQueries.insert(model), conn)(td).flatMap {
      case 1 => getByPrimaryKey(creds, model.id, conn)(td).map(_.map { n =>
        AuditHelper.onInsert("BottomRow", Seq(n.id.toString), n.toDataFields, creds)
        n
      })
      case _ => throw new IllegalStateException("Unable to find newly-inserted Bottom")
    })
  }
  def insertBatch(creds: Credentials, models: Seq[BottomRow], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insertBatch")(td => if (models.isEmpty) {
      Future.successful(0)
    } else {
      db.executeF(BottomRowQueries.insertBatch(models), conn)(td)
    })
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("create")(td => db.executeF(BottomRowQueries.create(fields), conn)(td).flatMap { _ =>
      AuditHelper.onInsert("BottomRow", Seq(fieldVal(fields, "id")), fields, creds)
      getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")), conn)
    })
  }

  def remove(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        AuditHelper.onRemove("BottomRow", Seq(id.toString), current.toDataFields, creds)
        db.executeF(BottomRowQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find BottomRow matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Bottom [$id]")
      case Some(current) => db.executeF(BottomRowQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, fields.find(_.k == "id").flatMap(_.v).map(s => UUID.fromString(s)).getOrElse(id), conn)(td).map {
          case Some(newModel) =>
            AuditHelper.onUpdate("BottomRow", Seq(id.toString), current.toDataFields, fields, creds)
            newModel -> s"Updated [${fields.size}] fields of Bottom [$id]"
          case None => throw new IllegalStateException(s"Cannot find BottomRow matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find BottomRow matching [$id]")
    })
  }

  def updateBulk(creds: Credentials, pks: Seq[UUID], fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    Future.sequence(pks.map(pk => update(creds, pk, fields, conn))).map { x =>
      s"Updated [${fields.size}] fields for [${x.size} of ${pks.size}] BottomRow"
    }
  }

  def csvFor(totalCount: Int, rows: Seq[BottomRow])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, BottomRowQueries.fields)(td))
  }
}
