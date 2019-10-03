/* Generated File */
package services.size

import com.kyleu.projectile.models.database.DatabaseFieldType
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.services.audit.AuditHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import java.sql.Connection
import models.queries.size.BigRowQueries
import models.size.BigRow
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class BigRowService @javax.inject.Inject() (val db: JdbcDatabase, override val tracing: TracingService)(implicit ec: ExecutionContext) extends ModelServiceHelper[BigRow]("bigRow", "size" -> "BigRow") {
  def getByPrimaryKey(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(BigRowQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = getByPrimaryKey(creds, id, conn).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load bigRow with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[Long], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(BigRowQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(BigRowQueries.countAll(filters), conn)(td))
  }
  override def getAll(creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(BigRowQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(BigRowQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(BigRowQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(BigRowQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countById(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id")(td => db.queryF(BigRowQueries.CountById(id), conn)(td))
  }
  def getById(creds: Credentials, id: Long, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id")(td => db.queryF(BigRowQueries.GetById(id, orderBys, limit, offset), conn)(td))
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[Long], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(BigRowQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByT(creds: Credentials, t: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.t")(td => db.queryF(BigRowQueries.CountByT(t), conn)(td))
  }
  def getByT(creds: Credentials, t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.t")(td => db.queryF(BigRowQueries.GetByT(t, orderBys, limit, offset), conn)(td))
  }
  def getByTSeq(creds: Credentials, tSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (tSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.t.seq") { td =>
        db.queryF(BigRowQueries.GetByTSeq(tSeq), conn)(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: BigRow, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insert")(td => db.queryF(BigRowQueries.insert(model), conn)(td).flatMap {
      case Some(pks) => getByPrimaryKey(creds, DatabaseFieldType.LongType.coerce(pks.head), conn)(td).map(_.map { n =>
        AuditHelper.onInsert("BigRow", Seq(n.id.toString), n.toDataFields, creds)
        n
      })
      case _ => throw new IllegalStateException("Unable to find newly-inserted Big")
    })
  }
  def insertBatch(creds: Credentials, models: Seq[BigRow], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insertBatch")(td => if (models.isEmpty) {
      Future.successful(0)
    } else {
      db.executeF(BigRowQueries.insertBatch(models), conn)(td)
    })
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("create")(td => db.queryF(BigRowQueries.create(fields), conn)(td).flatMap {
      case Some(pks) => getByPrimaryKey(creds, DatabaseFieldType.LongType.coerce(pks.head), conn)(td).map(_.map { n =>
        AuditHelper.onInsert("BigRow", Seq(n.id.toString), n.toDataFields, creds)
        n
      })
      case _ => throw new IllegalStateException("Unable to find newly-inserted Big")
    })
  }

  def remove(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        AuditHelper.onRemove("BigRow", Seq(id.toString), current.toDataFields, creds)
        db.executeF(BigRowQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find BigRow matching [$id]")
    })
  }

  def update(creds: Credentials, id: Long, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Big [$id]")
      case Some(current) => db.executeF(BigRowQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, fields.find(_.k == "id").flatMap(_.v).map(s => s.toLong).getOrElse(id), conn)(td).map {
          case Some(newModel) =>
            AuditHelper.onUpdate("BigRow", Seq(id.toString), current.toDataFields, fields, creds)
            newModel -> s"Updated [${fields.size}] fields of Big [$id]"
          case None => throw new IllegalStateException(s"Cannot find BigRow matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find BigRow matching [$id]")
    })
  }

  def updateBulk(creds: Credentials, pks: Seq[Long], fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    Future.sequence(pks.map(pk => update(creds, pk, fields, conn))).map { x =>
      s"Updated [${fields.size}] fields for [${x.size} of ${pks.size}] BigRow"
    }
  }

  def csvFor(totalCount: Int, rows: Seq[BigRow])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, BigRowQueries.fields)(td))
  }
}
