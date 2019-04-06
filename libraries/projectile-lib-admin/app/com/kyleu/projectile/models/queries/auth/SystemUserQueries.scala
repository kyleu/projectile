package com.kyleu.projectile.models.queries.auth

import java.util.UUID

import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.database._
import com.kyleu.projectile.models.queries.BaseQueries
import com.kyleu.projectile.models.user.{Role, SystemUser}
import com.mohiva.play.silhouette.api.LoginInfo

object SystemUserQueries extends BaseQueries[SystemUser]("systemUser", "system_user") {
  override val fields = Seq(
    DatabaseField(title = "Id", prop = "id", col = "id", typ = UuidType),
    DatabaseField(title = "Username", prop = "username", col = "username", typ = StringType),
    DatabaseField(title = "Provider", prop = "provider", col = "provider", typ = StringType),
    DatabaseField(title = "Key", prop = "key", col = "key", typ = StringType),
    DatabaseField(title = "Role", prop = "role", col = "role", typ = EnumType(Role)),
    DatabaseField(title = "Settings", prop = "settings", col = "settings", typ = JsonType),
    DatabaseField(title = "Created", prop = "created", col = "created", typ = TimestampType)
  )
  override protected val pkColumns = Seq("id")
  override protected val searchColumns = Seq("id", "username", "provider", "key")

  def getByPrimaryKey(id: UUID) = new GetByPrimaryKey(Seq(id))
  def getByPrimaryKeySeq(idSeq: Seq[UUID]) = new ColSeqQuery(column = "id", values = idSeq)

  def insert(model: SystemUser) = new Insert(model)

  def removeByPrimaryKey(id: UUID) = new RemoveByPrimaryKey(Seq[Any](id))

  final case class UpdateUser(u: SystemUser) extends Statement {
    override val name = s"$key.update.user"
    override val sql = updateSql(Seq("username", "provider", "key", "role", "settings"))
    override val values = Seq[Any](u.username, u.profile.providerID, u.profile.providerKey, u.role.toString, u.settings, u.id)
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

  override protected def fromRow(row: Row) = {
    val id = UuidType(row, "id")
    val username = StringType(row, "username")
    val profile = LoginInfo(StringType(row, "provider"), StringType(row, "key"))
    val role = Role.withValue(StringType(row, "role").trim)
    val settings = JsonType(row, "settings")
    val created = TimestampType(row, "created")
    SystemUser(id, username, profile, role, settings, created)
  }

  override protected def toDataSeq(u: SystemUser) = Seq[Any](
    u.id.toString, u.username, u.profile.providerID, u.profile.providerKey, u.role.toString, u.settings, u.created
  )
}
