package com.kyleu.projectile.services.feedback

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, CsvUtils}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.feedback.Feedback
import com.kyleu.projectile.models.queries.feedback.FeedbackQueries
import javax.inject.Named

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class FeedbackService @javax.inject.Inject() (
    override val tracing: TracingService, @Named("system") db: JdbcDatabase
)(implicit ec: ExecutionContext) extends ModelServiceHelper[Feedback]("feedback", "feedback" -> "Feedback") {
  def getByPrimaryKey(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.primary.key")(td => db.queryF(FeedbackQueries.getByPrimaryKey(id))(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID)(implicit trace: TraceData) = getByPrimaryKey(creds, id).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load feedback with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(FeedbackQueries.getByPrimaryKeySeq(idSeq))(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(FeedbackQueries.countAll(filters))(td))
  }
  override def getAll(creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(FeedbackQueries.getAll(filters, orderBys, limit, offset))(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(FeedbackQueries.searchCount(q, filters))(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(FeedbackQueries.search(q, filters, orderBys, limit, offset))(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(FeedbackQueries.searchExact(q, orderBys, limit, offset))(td))
  }

  def countByAuthorEmail(creds: Credentials, authorEmail: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.authorEmail") { td =>
      db.queryF(FeedbackQueries.CountByAuthorEmail(authorEmail))(td)
    }
  }
  def getByAuthorEmail(creds: Credentials, authorEmail: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.authorEmail") { td =>
      db.queryF(FeedbackQueries.GetByAuthorEmail(authorEmail, orderBys, limit, offset))(td)
    }
  }
  def getByAuthorEmailSeq(creds: Credentials, authorEmailSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (authorEmailSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.authorEmail.seq") { td =>
        db.queryF(FeedbackQueries.GetByAuthorEmailSeq(authorEmailSeq))(td)
      }
    }
  }

  def countByAuthorId(creds: Credentials, authorId: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.authorId") { td =>
      db.queryF(FeedbackQueries.CountByAuthorId(authorId))(td)
    }
  }
  def getByAuthorId(creds: Credentials, authorId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.authorId") { td =>
      db.queryF(FeedbackQueries.GetByAuthorId(authorId, orderBys, limit, offset))(td)
    }
  }
  def getByAuthorIdSeq(creds: Credentials, authorIdSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (authorIdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.authorId.seq") { td =>
        db.queryF(FeedbackQueries.GetByAuthorIdSeq(authorIdSeq))(td)
      }
    }
  }

  def countByCreated(creds: Credentials, created: LocalDateTime)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.created") { td =>
      db.queryF(FeedbackQueries.CountByCreated(created))(td)
    }
  }
  def getByCreated(creds: Credentials, created: LocalDateTime, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.created") { td =>
      db.queryF(FeedbackQueries.GetByCreated(created, orderBys, limit, offset))(td)
    }
  }
  def getByCreatedSeq(creds: Credentials, createdSeq: Seq[LocalDateTime])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (createdSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.created.seq") { td =>
        db.queryF(FeedbackQueries.GetByCreatedSeq(createdSeq))(td)
      }
    }
  }

  def countById(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id") { td =>
      db.queryF(FeedbackQueries.CountById(id))(td)
    }
  }
  def getById(creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id") { td =>
      db.queryF(FeedbackQueries.GetById(id, orderBys, limit, offset))(td)
    }
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(FeedbackQueries.GetByIdSeq(idSeq))(td)
      }
    }
  }

  def countByStatus(creds: Credentials, status: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.status") { td =>
      db.queryF(FeedbackQueries.CountByStatus(status))(td)
    }
  }
  def getByStatus(creds: Credentials, status: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.status") { td =>
      db.queryF(FeedbackQueries.GetByStatus(status, orderBys, limit, offset))(td)
    }
  }
  def getByStatusSeq(creds: Credentials, statusSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (statusSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.status.seq") { td =>
        db.queryF(FeedbackQueries.GetByStatusSeq(statusSeq))(td)
      }
    }
  }

  def countByText(creds: Credentials, text: String)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.text") { td =>
      db.queryF(FeedbackQueries.CountByText(text))(td)
    }
  }
  def getByText(creds: Credentials, text: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.text") { td =>
      db.queryF(FeedbackQueries.GetByText(text, orderBys, limit, offset))(td)
    }
  }
  def getByTextSeq(creds: Credentials, textSeq: Seq[String])(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (textSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.text.seq") { td =>
        db.queryF(FeedbackQueries.GetByTextSeq(textSeq))(td)
      }
    }
  }

  // Mutations
  def insert(creds: Credentials, model: Feedback)(implicit trace: TraceData) = {
    traceF("insert") { td =>
      db.executeF(FeedbackQueries.insert(model))(td).flatMap {
        case 1 => getByPrimaryKey(creds, model.id)(td)
        case _ => throw new IllegalStateException("Unable to find newly-inserted Feedback.")
      }
    }
  }
  def insertBatch(creds: Credentials, models: Seq[Feedback])(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(FeedbackQueries.insertBatch(models))(td))
  }
  def create(creds: Credentials, fields: Seq[DataField])(implicit trace: TraceData) = {
    traceF("create") { td =>
      db.executeF(FeedbackQueries.create(fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")))
      }
    }
  }

  def remove(creds: Credentials, id: UUID)(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("remove")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) =>
        db.executeF(FeedbackQueries.removeByPrimaryKey(id))(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find Feedback matching [$id]")
    })
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField])(implicit trace: TraceData) = checkPerm(creds, "edit") {
    traceF("update")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for Feedback [$id]")
      case Some(_) => db.executeF(FeedbackQueries.update(id, fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, fields.find(_.k == "id").flatMap(_.v).map(s => UUID.fromString(s)).getOrElse(id))(td).map {
          case Some(newModel) =>
            newModel -> s"Updated [${fields.size}] fields of Feedback [$id]"
          case None => throw new IllegalStateException(s"Cannot find Feedback matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find Feedback matching [$id]")
    })
  }

  def csvFor(totalCount: Int, rows: Seq[Feedback])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, FeedbackQueries.fields)(td))
  }
}
