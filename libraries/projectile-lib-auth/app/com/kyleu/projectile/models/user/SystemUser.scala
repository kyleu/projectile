package com.kyleu.projectile.models.user

import java.time.LocalDateTime
import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object SystemUser {
  private[this] implicit val jsonLoginInfoEncoder: Encoder[LoginInfo] = deriveEncoder
  private[this] implicit val jsonLoginInfoDecoder: Decoder[LoginInfo] = deriveDecoder

  implicit val jsonEncoder: Encoder[SystemUser] = deriveEncoder
  implicit val jsonDecoder: Decoder[SystemUser] = deriveDecoder

  def empty() = SystemUser(
    id = UUID.randomUUID,
    username = "",
    profile = LoginInfo("anonymous", "guest")
  )

  val system = SystemUser(
    id = UUID.fromString("88888888-8888-8888-8888-888888888888"),
    username = "",
    profile = LoginInfo("anonymous", "system"),
    role = Role.Admin
  )

  val guest = SystemUser(
    id = UUID.fromString("77777777-7777-7777-7777-777777777777"),
    username = "guest",
    profile = LoginInfo("anonymous", "guest")
  )

  val api = SystemUser(
    id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
    username = "api",
    profile = LoginInfo("anonymous", "api"),
    role = Role.Admin
  )
}

final case class SystemUser(
    id: UUID,
    username: String,
    profile: LoginInfo,
    role: Role = Role.User,
    settings: Json = JsonObject.empty.asJson,
    created: LocalDateTime = DateUtils.now
) extends Identity with DataFieldModel {

  def email = profile.providerKey
  def provider = profile.providerID
  def key = profile.providerKey

  def isAdmin = role == Role.Admin

  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("username", Some(username)),
    DataField("provider", Some(profile.providerID)),
    DataField("key", Some(profile.providerKey)),
    DataField("role", Some(role.toString)),
    DataField("created", Some(created.toString))
  )

  def toSummary = DataSummary(model = "systemUser", pk = id.toString, title = s"$username: $role")
}
