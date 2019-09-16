// scalastyle:off file.size.limit
package com.kyleu.projectile.services.user

import java.sql.Connection

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.services.audit.AuditHelper
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.{Credentials, CsvUtils}
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
)(implicit ec: ExecutionContext) extends ModelServiceHelper[SystemUser]("systemUser", "models" -> "SystemUser") {
  def getByPrimaryKey(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("get.by.primary.key")(td => db.queryF(SystemUserQueries.getByPrimaryKey(id), conn)(td))
  }
  def getByPrimaryKeyRequired(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    getByPrimaryKey(creds, id, conn).map { opt =>
      opt.getOrElse(throw new IllegalStateException(s"Cannot load systemUser with id [$id]"))
    }
  }
  def getByPrimaryKeySeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.primary.key.seq")(td => db.queryF(SystemUserQueries.getByPrimaryKeySeq(idSeq), conn)(td))
    }
  }

  override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all.count")(td => db.queryF(SystemUserQueries.countAll(filters), conn)(td))
  }
  override def getAll(
    creds: Credentials, filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.all")(td => db.queryF(SystemUserQueries.getAll(filters, orderBys, limit, offset), conn)(td))
  }

  // Search
  override def searchCount(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.count")(td => db.queryF(SystemUserQueries.searchCount(q, filters), conn)(td))
  }
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil,
    limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search")(td => db.queryF(SystemUserQueries.search(q, filters, orderBys, limit, offset), conn)(td))
  }

  def searchExact(
    creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("search.exact")(td => db.queryF(SystemUserQueries.searchExact(q, orderBys, limit, offset), conn)(td))
  }

  def countById(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.id") { td =>
      db.queryF(SystemUserQueries.CountById(id), conn)(td)
    }
  }
  def getById(
    creds: Credentials, id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.id") { td =>
      db.queryF(SystemUserQueries.GetById(id, orderBys, limit, offset), conn)(td)
    }
  }
  def getByIdSeq(creds: Credentials, idSeq: Seq[UUID], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (idSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.id.seq") { td =>
        db.queryF(SystemUserQueries.GetByIdSeq(idSeq), conn)(td)
      }
    }
  }

  def countByKey(creds: Credentials, key: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.key") { td =>
      db.queryF(SystemUserQueries.CountByKey(key), conn)(td)
    }
  }
  def getByKey(
    creds: Credentials, key: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.key") { td =>
      db.queryF(SystemUserQueries.GetByKey(key, orderBys, limit, offset), conn)(td)
    }
  }
  def getByKeySeq(creds: Credentials, keySeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (keySeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.key.seq") { td =>
        db.queryF(SystemUserQueries.GetByKeySeq(keySeq), conn)(td)
      }
    }
  }

  def countByProvider(creds: Credentials, provider: String, conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("count.by.provider") { td =>
      db.queryF(SystemUserQueries.CountByProvider(provider), conn)(td)
    }
  }
  def getByProvider(
    creds: Credentials, provider: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = checkPerm(creds, "view") {
    traceF("get.by.provider") { td =>
      db.queryF(SystemUserQueries.GetByProvider(provider, orderBys, limit, offset), conn)(td)
    }
  }
  def getByProviderSeq(creds: Credentials, providerSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = checkPerm(creds, "view") {
    if (providerSeq.isEmpty) {
      Future.successful(Nil)
    } else {
      traceF("get.by.provider.seq") { td =>
        db.queryF(SystemUserQueries.GetByProviderSeq(providerSeq), conn)(td)
      }
    }
  }

  def countByUsername(creds: Credentials, username: String, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("count.by.username") { td =>
      db.queryF(SystemUserQueries.CountByUsername(username), conn)(td)
    }
  }
  def findByUsername(creds: Credentials, username: String, conn: Option[Connection] = None)(implicit trace: TraceData) = tracing.trace("get.by.username") { td =>
    db.queryF(SystemUserQueries.FindUserByUsername(username), conn)(td)
  }
  def isUsernameInUse(creds: Credentials, name: String, conn: Option[Connection] = None)(implicit trace: TraceData) = tracing.trace("username.in.use") { td =>
    db.queryF(UserSearchQueries.IsUsernameInUse(name), conn)(td)
  }
  def getByUsername(
    creds: Credentials, username: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData) = traceF("get.by.username") { td =>
    db.queryF(SystemUserQueries.GetByUsername(username, orderBys, limit, offset), conn)(td)
  }
  def getByUsernameSeq(creds: Credentials, usernameSeq: Seq[String], conn: Option[Connection] = None)(implicit trace: TraceData) = if (usernameSeq.isEmpty) {
    Future.successful(Nil)
  } else {
    traceF("get.by.username.seq") { td =>
      db.queryF(SystemUserQueries.GetByUsernameSeq(usernameSeq), conn)(td)
    }
  }

  // Mutations
  def insert(creds: Credentials, model: SystemUser, conn: Option[Connection] = None)(implicit trace: TraceData) = traceF("insert") { td =>
    db.executeF(SystemUserQueries.insert(model), conn)(td).flatMap {
      case 1 => getByPrimaryKey(creds, model.id, conn)(td).map {
        case Some(n) =>
          AuditHelper.onInsert("SystemUser", Seq(n.id.toString), n.toDataFields, creds)
          model
        case None => throw new IllegalStateException(s"Unable to find System User with id [${model.id}]")
      }
      case _ => throw new IllegalStateException(s"Unable to find newly-inserted System User with id [${model.id}]")
    }
  }
  def insertBatch(creds: Credentials, models: Seq[SystemUser], conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("insertBatch")(td => db.executeF(SystemUserQueries.insertBatch(models), conn)(td))
  }
  def create(creds: Credentials, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = traceF("create") { td =>
    db.executeF(SystemUserQueries.create(fields), conn)(td).flatMap { _ =>
      AuditHelper.onInsert("SystemUser", Seq(fieldVal(fields, "id")), fields, creds)
      getByPrimaryKey(creds, UUID.fromString(fieldVal(fields, "id")), conn)
    }
  }

  def remove(creds: Credentials, id: UUID, conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("remove")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) =>
        AuditHelper.onRemove("SystemUser", Seq(id.toString), current.toDataFields, creds)
        UserCache.removeUser(id)
        db.executeF(SystemUserQueries.removeByPrimaryKey(id), conn)(td).map(_ => current)
      case None => throw new IllegalStateException(s"Cannot find SystemUser matching [$id]")
    })
  }

  def updateUser(creds: Credentials, model: SystemUser, conn: Option[Connection] = None)(implicit trace: TraceData) = tracing.trace("update") { td =>
    update(creds, model.id, model.toDataFields, conn)(td).map { _ =>
      UserCache.cacheUser(model)
      model
    }
  }

  def update(creds: Credentials, id: UUID, fields: Seq[DataField], conn: Option[Connection] = None)(implicit trace: TraceData) = {
    traceF("update")(td => getByPrimaryKey(creds, id, conn)(td).flatMap {
      case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for System User [$id]")
      case Some(current) => db.executeF(SystemUserQueries.update(id, fields), conn)(td).flatMap { _ =>
        getByPrimaryKey(creds, id, conn)(td).map {
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
