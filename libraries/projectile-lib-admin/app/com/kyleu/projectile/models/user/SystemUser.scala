package com.kyleu.projectile.models.user

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.config.UserSettings
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object SystemUser {
  implicit val jsonLoginInfoEncoder: Encoder[LoginInfo] = deriveEncoder
  implicit val jsonLoginInfoDecoder: Decoder[LoginInfo] = deriveDecoder

  implicit val jsonEncoder: Encoder[SystemUser] = deriveEncoder
  implicit val jsonDecoder: Decoder[SystemUser] = deriveDecoder

  def empty() = SystemUser(
    id = UUID.randomUUID,
    username = "",
    profile = LoginInfo("anonymous", "guest"),
    role = "user"
  )

  val system = SystemUser(
    id = UUID.fromString("88888888-8888-8888-8888-888888888888"),
    username = "",
    profile = LoginInfo("anonymous", "system"),
    role = "admin"
  )

  val guest = SystemUser(
    id = UUID.fromString("77777777-7777-7777-7777-777777777777"),
    username = "guest",
    profile = LoginInfo("anonymous", "guest"),
    role = "user"
  )

  val api = SystemUser(
    id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
    username = "api",
    profile = LoginInfo("anonymous", "api"),
    role = "admin"
  )
}

final case class SystemUser(
    id: UUID,
    username: String,
    profile: LoginInfo,
    role: String,
    settings: Json = JsonObject.empty.asJson,
    created: LocalDateTime = DateUtils.now
) extends Identity with DataFieldModel {

  val email = profile.providerKey
  val provider = profile.providerID
  val key = profile.providerKey

  lazy val settingsObj = extract[UserSettings](settings)

  def isAdmin = role == "admin"

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
