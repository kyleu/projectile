/* Generated File */
package services.size

import com.kyleu.projectile.models.database.DatabaseFieldType
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import java.sql.Connection
import models.queries.size.SmallRowQueries
import models.size.SmallRow
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SmallRowService @javax.inject.Inject() (val db: JdbcDatabase, override val tracing: TracingService)(implicit ec: ExecutionContext) extends ModelServiceHelper[SmallRow]("smallRow", "size" -> "SmallRow") {
  def getByPrimaryKey(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(SmallRowQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = getByPrimaryKey(creds, id, conn).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load smallRow with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[Long], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(SmallRowQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(SmallRowQueries.countAll(filters), conn)(td))
  }
  override def getAll(creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(SmallRowQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(SmallRowQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(SmallRowQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(SmallRowQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countByBigId(creds: Credentials, bigId: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.bigId")(td => db.queryF(SmallRowQueries.CountByBigId(bigId), conn)(td))
  }
  def getByBigId(creds: Credentials, bigId: Long, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.bigId")(td => db.queryF(SmallRowQueries.GetByBigId(bigId, orderBys, limit, offset), conn)(td))
  }
  def getByBigIdSeq(creds: Credentials, bigIdSeq: Seq[Long], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (bigIdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.bigId.seq") { td =>
        db.queryF(SmallRowQueries.GetByBigIdSeq(bigIdSeq), conn)(td)
      }
    }
  }

  def countById(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id")(td => db.queryF(SmallRowQueries.CountById(id), conn)(td))
  }
  def getById(creds: Credentials, id: Long, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id")(td => db.queryF(SmallRowQueries.GetById(id, orderBys, limit, offset), conn)(td))
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[Long], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(SmallRowQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByT(creds: Credentials, t: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.t")(td => db.queryF(SmallRowQueries.CountByT(t), conn)(td))
  }
  def getByT(creds: Credentials, t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.t")(td => db.queryF(SmallRowQueries.GetByT(t, orderBys, limit, offset), conn)(td))
  }
  def getByTSeq(creds: Credentials, tSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (tSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.t.seq") { td =>
        db.queryF(SmallRowQueries.GetByTSeq(tSeq), conn)(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: SmallRow, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insert")(td => db.queryF(SmallRowQueries.insert(model), conn)(td).flatMap {
      case Some(pks) => getByPrimaryKey(creds, DatabaseFieldType.LongType.coerce(pks.head), conn)(td)
      case _ => throw new IllegalStateException("Unable to find newly-inserted Small")
    })
  }
  def insertBatch(creds: Credentials, models: Seq[SmallRow], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("insertBatch")(td => if (models.isEmpty) {
      Future.successful(0)
    } else {
      Future.sequence(models.grouped(100).zipWithIndex.map { batch =>
        log.info(s"Processing batch [${batch._2}]...")
        db.executeF(SmallRowQueries.insertBatch(batch._1), conn)(td)
      }).map(_.sum)
    })
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("create")(td => db.queryF(SmallRowQueries.create(fields), conn)(td).flatMap {
      case Some(pks) => getByPrimaryKey(creds, DatabaseFieldType.LongType.coerce(pks.head), conn)(td)
      case _ => throw new IllegalStateException("Unable to find newly-inserted Small")
    })
  }

  def remove(creds: Credentials, id: Long, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        db.executeF(SmallRowQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find SmallRow matching [$id]")
    })
  }

  def update(creds: Credentials, id: Long, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Small [$id]")
      case Some(_) => db.executeF(SmallRowQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, fields.find(_.k == "id").flatMap(_.v).map(s => s.toLong).getOrElse(id), conn)(td).map {
          case Some(newModel) =>
            newModel -> s"Updated [${fields.size}] fields of Small [$id]"
          case None => throw new IllegalStateException(s"Cannot find SmallRow matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find SmallRow matching [$id]")
    })
  }

  def updateBulk(creds: Credentials, pks: Seq[Long], fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    Future.sequence(pks.map(pk => update(creds, pk, fields, conn))).map { x =>
      s"Updated [${fields.size}] fields for [${x.size} of ${pks.size}] SmallRow"
    }
  }

  def csvFor(totalCount: Int, rows: Seq[SmallRow])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, SmallRowQueries.fields)(td))
  }
}
