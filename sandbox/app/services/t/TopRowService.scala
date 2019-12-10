/* Generated File */
package services.t

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
import models.queries.t.TopRowQueries
import models.t.TopRow
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class TopRowService @javax.inject.Inject() (val db: JdbcDatabase, override val tracing: TracingService)(implicit ec: ExecutionContext) extends ModelServiceHelper[TopRow]("topRow", "t" -> "TopRow") {
  def getByPrimaryKey(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(TopRowQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = getByPrimaryKey(creds, id, conn).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load topRow with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(TopRowQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(TopRowQueries.countAll(filters), conn)(td))
  }
  override def getAll(creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(TopRowQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(TopRowQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(TopRowQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(TopRowQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countById(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id")(td => db.queryF(TopRowQueries.CountById(id), conn)(td))
  }
  def getById(creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id")(td => db.queryF(TopRowQueries.GetById(id, orderBys, limit, offset), conn)(td))
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(TopRowQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByT(creds: Credentials, t: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.t")(td => db.queryF(TopRowQueries.CountByT(t), conn)(td))
  }
  def getByT(creds: Credentials, t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.t")(td => db.queryF(TopRowQueries.GetByT(t, orderBys, limit, offset), conn)(td))
  }
  def getByTSeq(creds: Credentials, tSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (tSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.t.seq") { td =>
        db.queryF(TopRowQueries.GetByTSeq(tSeq), conn)(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: TopRow, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insert")(td => db.executeF(TopRowQueries.insert(model), conn)(td).flatMap {
      case 1 => getByPrimaryKey(creds, model.id, conn)(td).map(_.map { n =>
        AuditHelper.onInsert("TopRow", Seq(n.id.toString), n.toDataFields, creds)
        n
      })
      case _ => throw new IllegalStateException("Unable to find newly-inserted Top")
    })
  }
  def insertBatch(creds: Credentials, models: Seq[TopRow], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insertBatch")(td => if (models.isEmpty) {
      Future.successful(0)
    } else {
      Future.sequence(models.grouped(100).zipWithIndex.map { batch =>
        log.info(s"Processing batch [${batch._2}]...")
        db.executeF(TopRowQueries.insertBatch(batch._1), conn)(td)
      }).map(_.sum)
    })
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("create")(td => db.executeF(TopRowQueries.create(fields), conn)(td).flatMap { _ =>
      AuditHelper.onInsert("TopRow", Seq(fieldVal(fields, "id")), fields, creds)
      getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")), conn)
    })
  }

  def remove(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        AuditHelper.onRemove("TopRow", Seq(id.toString), current.toDataFields, creds)
        db.executeF(TopRowQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find TopRow matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Top [$id]")
      case Some(current) => db.executeF(TopRowQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, fields.find(_.k == "id").flatMap(_.v).map(s => UUID.fromString(s)).getOrElse(id), conn)(td).map {
          case Some(newModel) =>
            AuditHelper.onUpdate("TopRow", Seq(id.toString), current.toDataFields, fields, creds)
            newModel -> s"Updated [${fields.size}] fields of Top [$id]"
          case None => throw new IllegalStateException(s"Cannot find TopRow matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find TopRow matching [$id]")
    })
  }

  def updateBulk(creds: Credentials, pks: Seq[UUID], fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    Future.sequence(pks.map(pk => update(creds, pk, fields, conn))).map { x =>
      s"Updated [${fields.size}] fields for [${x.size} of ${pks.size}] TopRow"
    }
  }

  def csvFor(totalCount: Int, rows: Seq[TopRow])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, TopRowQueries.fields)(td))
  }
}
