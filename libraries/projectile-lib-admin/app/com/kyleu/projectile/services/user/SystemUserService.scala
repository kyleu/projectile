package com.kyleu.projectile.services.user

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.{Credentials, ModelServiceHelper}
import com.kyleu.projectile.services.audit.AuditHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.CsvUtils
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import java.util.UUID

import com.google.inject.name.Named
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.models.queries.auth.{SystemUserQueries, UserSearchQueries}
import com.kyleu.projectile.services.cache.UserCache

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SystemUserService @javax.inject.Inject() (
    @Named("system") val db: JdbcDatabase,
    override val tracing: TracingService
)(implicit ec: ExecutionContext) extends ModelServiceHelper[SystemUser]("systemUser") {
  def getByPrimaryKey(creds: Credentials, id: UUID)(implicit trace: TraceData) = {
    traceF("get.by.primary.key")(td => db.queryF(SystemUserQueries.getByPrimaryKey(id))(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID)(implicit trace: TraceData) = getByPrimaryKey(creds, id).map { opt =>
    opt.getOrElse(throw new IllegalStateException(s"Cannot load systemUser with id [$id]"))
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = if (idSeq.isEmpty) {
    Future.successful(Nil)
  } else {
    traceF("get.by.primary.key.seq")(td => db.queryF(SystemUserQueries.getByPrimaryKeySeq(idSeq))(td))
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil)(implicit trace: TraceData) = {
    traceF("get.all.count")(td => db.queryF(SystemUserQueries.countAll(filters))(td))
  }
  override def getAll(creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = {
    traceF("get.all")(td => db.queryF(SystemUserQueries.getAll(filters, orderBys, limit, offset))(td))
  }

  // Search
  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil)(implicit trace: TraceData) = {
    traceF("search.count")(td => db.queryF(SystemUserQueries.searchCount(q, filters))(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = {
    traceF("search")(td => db.queryF(SystemUserQueries.search(q, filters, orderBys, limit, offset))(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = {
    traceF("search.exact")(td => db.queryF(SystemUserQueries.searchExact(q, orderBys, limit, offset))(td))
  }

  def countById(creds: Credentials, id: UUID)(implicit trace: TraceData) = traceF("count.by.id") { td =>
    db.queryF(SystemUserQueries.CountById(id))(td)
  }
  def getById(creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = traceF("get.by.id") { td =>
    db.queryF(SystemUserQueries.GetById(id, orderBys, limit, offset))(td)
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID])(implicit trace: TraceData) = if (idSeq.isEmpty) {
    Future.successful(Nil)
  } else {
    traceF("get.by.id.seq") { td =>
      db.queryF(SystemUserQueries.GetByIdSeq(idSeq))(td)
    }
  }

  def countByKey(creds: Credentials, key: String)(implicit trace: TraceData) = traceF("count.by.key") { td =>
    db.queryF(SystemUserQueries.CountByKey(key))(td)
  }
  def getByKey(creds: Credentials, key: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)(implicit trace: TraceData) = traceF("get.by.key") { td =>
    db.queryF(SystemUserQueries.GetByKey(key, orderBys, limit, offset))(td)
  }
  def getByKeySeq(creds: Credentials, keySeq: Seq[String])(implicit trace: TraceData) = if (keySeq.isEmpty) {
    Future.successful(Nil)
  } else {
    traceF("get.by.key.seq") { td =>
      db.queryF(SystemUserQueries.GetByKeySeq(keySeq))(td)
    }
  }

  def countByProvider(creds: Credentials, provider: String)(implicit trace: TraceData) = traceF("count.by.provider") { td =>
    db.queryF(SystemUserQueries.CountByProvider(provider))(td)
  }
  def getByProvider(
    creds: Credentials, provider: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = traceF("get.by.provider") { td =>
    db.queryF(SystemUserQueries.GetByProvider(provider, orderBys, limit, offset))(td)
  }
  def getByProviderSeq(creds: Credentials, providerSeq: Seq[String])(implicit trace: TraceData) = if (providerSeq.isEmpty) {
    Future.successful(Nil)
  } else {
    traceF("get.by.provider.seq") { td =>
      db.queryF(SystemUserQueries.GetByProviderSeq(providerSeq))(td)
    }
  }

  def countByUsername(creds: Credentials, username: String)(implicit trace: TraceData) = traceF("count.by.username") { td =>
    db.queryF(SystemUserQueries.CountByUsername(username))(td)
  }
  def findByUsername(creds: Credentials, username: String)(implicit trace: TraceData) = tracing.trace("get.by.username") { td =>
    db.queryF(SystemUserQueries.FindUserByUsername(username))(td)
  }
  def isUsernameInUse(creds: Credentials, name: String)(implicit trace: TraceData) = tracing.trace("username.in.use") { td =>
    db.queryF(UserSearchQueries.IsUsernameInUse(name))(td)
  }
  def getByUsername(
    creds: Credentials, username: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData) = traceF("get.by.username") { td =>
    db.queryF(SystemUserQueries.GetByUsername(username, orderBys, limit, offset))(td)
  }
  def getByUsernameSeq(creds: Credentials, usernameSeq: Seq[String])(implicit trace: TraceData) = if (usernameSeq.isEmpty) {
    Future.successful(Nil)
  } else {
    traceF("get.by.username.seq") { td =>
      db.queryF(SystemUserQueries.GetByUsernameSeq(usernameSeq))(td)
    }
  }

  // Mutations
  def insert(creds: Credentials, model: SystemUser)(implicit trace: TraceData) = traceF("insert") { td =>
    db.executeF(SystemUserQueries.insert(model))(td).flatMap {
      case 1 => getByPrimaryKey(creds, model.id)(td).map {
        case Some(n) =>
          AuditHelper.onInsert("SystemUser", Seq(n.id.toString), n.toDataFields, creds)
          model
        case None => throw new IllegalStateException("Unable to find System User.")
      }
      case _ => throw new IllegalStateException("Unable to find newly-inserted System User.")
    }
  }
  def insertBatch(creds: Credentials, models: Seq[SystemUser])(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(SystemUserQueries.insertBatch(models))(td))
  }
  def create(creds: Credentials, fields: Seq[DataField])(implicit trace: TraceData) = traceF("create") { td =>
    db.executeF(SystemUserQueries.create(fields))(td).flatMap { _ =>
      AuditHelper.onInsert("SystemUser", Seq(fieldVal(fields, "id")), fields, creds)
      getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")))
    }
  }

  def remove(creds: Credentials, id: UUID)(implicit trace: TraceData) = {
    traceF("remove")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) =>
        AuditHelper.onRemove("SystemUser", Seq(id.toString), current.toDataFields, creds)
        UserCache.removeUser(id)
        db.executeF(SystemUserQueries.removeByPrimaryKey(id))(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find SystemUser matching [$id]")
    })
  }

  def updateUser(creds: Credentials, model: SystemUser)(implicit trace: TraceData) = tracing.trace("update") { td =>
    update(creds, model.id, model.toDataFields)(td).map { _ =>
      UserCache.cacheUser(model)
      model
    }
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField])(implicit trace: TraceData) = {
    traceF("update")(td => getByPrimaryKey(creds, id)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for System User [$id]")
      case Some(current) => db.executeF(SystemUserQueries.update(id, fields))(td).flatMap { _ =>
        getByPrimaryKey(creds, id)(td).map {
          case Some(newModel) =>
            AuditHelper.onUpdate("SystemUser", Seq(id.toString), current.toDataFields, fields, creds)
            UserCache.cacheUser(newModel)
            newModel -> s"Updated [${fields.size}] fields of System User [$id]"
          case None => throw new IllegalStateException(s"Cannot find SystemUser matching [$id]")
        }
      }
      case None => throw new IllegalStateException(s"Cannot find SystemUser matching [$id]")
    })
  }

  def csvFor(totalCount: Int, rows: Seq[SystemUser])(implicit trace: TraceData) = {
    traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, SystemUserQueries.fields)(td))
  }
}
