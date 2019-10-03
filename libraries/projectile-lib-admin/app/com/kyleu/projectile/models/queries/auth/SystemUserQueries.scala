// scalastyle:off file.size.limit
package com.kyleu.projectile.models.queries.auth

import java.util.UUID

import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.database._
import com.kyleu.projectile.models.queries.{BaseQueries, ResultFieldHelper}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.user.{LoginCredentials, SystemUser}
import com.mohiva.play.silhouette.api.LoginInfo

object SystemUserQueries extends BaseQueries[SystemUser]("systemUser", "system_user") {
  override val fields = Seq(
    DatabaseField(title = "Id", prop = "id", col = "id", typ = UuidType),
    DatabaseField(title = "Username", prop = "username", col = "username", typ = StringType),
    DatabaseField(title = "Provider", prop = "provider", col = "provider", typ = StringType),
    DatabaseField(title = "Key", prop = "key", col = "key", typ = StringType),
    DatabaseField(title = "Role", prop = "role", col = "role", typ = StringType),
    DatabaseField(title = "Settings", prop = "settings", col = "settings", typ = JsonType),
    DatabaseField(title = "Created", prop = "created", col = "created", typ = TimestampType)
  )
  override val pkColumns = Seq("id")
  override protected val searchColumns = Seq("id", "username", "provider", "key")

  def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)
  def getAll(filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new GetAll(filters, orderBys, limit, offset)
  }

  def search(q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new Search(q, filters, orderBys, limit, offset)
  }
  def searchCount(q: Option[String], filters: Seq[Filter] = Nil) = new SearchCount(q, filters)
  def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = new SearchExact(q, orderBys, limit, offset)

  def getByPrimaryKey(id: UUID) = new GetByPrimaryKey(Seq(id))
  def getByPrimaryKeySeq(idSeq: Seq[UUID]) = new ColSeqQuery(column = "id", values = idSeq)

  final case class UpdateUser(u: SystemUser) extends Statement {
    override val name = s"$key.update.user"
    override val sql = updateSql(Seq("username", "provider", "key", "role", "settings"))
    override val values = Seq[Any](u.username, u.profile.providerID, u.profile.providerKey, u.role, u.settings, u.id)
  }

  final case class FindUserByUsername(username: String) extends FlatSingleRowQuery[SystemUser] {
    override val name = s"$key.find.by.username"
    override val sql = getSql(Some(quote("username") + " = ?"))
    override val values = Seq(username)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  final case class FindUserByProfile(loginInfo: LoginInfo) extends FlatSingleRowQuery[SystemUser] {
    override val name = s"$key.find.by.profile"
    override val sql = getSql(Some(quote("provider") + " = ? and " + quote("key") + " = ?"))
    override val values = Seq(loginInfo.providerID, loginInfo.providerKey)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  final case class CountById(id: UUID) extends ColCount(column = "id", values = Seq(id))
  final case class GetById(id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(id)
  )
  final case class GetByIdSeq(idSeq: Seq[UUID]) extends ColSeqQuery(column = "id", values = idSeq)

  final case class CountByKey(key: String) extends ColCount(column = "key", values = Seq(key))
  final case class GetByKey(key: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("key") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(key)
  )
  final case class GetByKeySeq(keySeq: Seq[String]) extends ColSeqQuery(column = "key", values = keySeq)

  final case class CountByProvider(provider: String) extends ColCount(column = "provider", values = Seq(provider))
  final case class GetByProvider(provider: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("provider") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(provider)
  )
  final case class GetByProviderSeq(providerSeq: Seq[String]) extends ColSeqQuery(column = "provider", values = providerSeq)

  final case class CountByUsername(username: String) extends ColCount(column = "username", values = Seq(username))
  final case class GetByUsername(username: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("username") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(username)
  )
  final case class GetByUsernameSeq(usernameSeq: Seq[String]) extends ColSeqQuery(column = "username", values = usernameSeq)

  def insert(model: SystemUser) = new Insert(model)
  def insertBatch(models: Seq[SystemUser]) = new InsertBatch(models)
  def create(dataFields: Seq[DataField]) = new InsertFields(dataFields)

  def removeByPrimaryKey(id: UUID) = new RemoveByPrimaryKey(Seq[Any](id))

  def update(id: UUID, fields: Seq[DataField]) = new UpdateFields(Seq[Any](id), fields)

  override protected def fromRow(row: Row) = {
    val id = UuidType(row, "id")
    val username = StringType(row, "username")
    val profile = LoginCredentials(StringType(row, "provider"), StringType(row, "key"))
    val role = StringType(row, "role")
    val settings = JsonType(row, "settings")
    val created = TimestampType(row, "created")
    SystemUser(id, username, profile, role, settings, created)
  }

  override protected def toDataSeq(u: SystemUser) = Seq[Any](
    u.id.toString, u.username, u.profile.providerID, u.profile.providerKey, u.role.toString, u.settings, u.created
  )
}
