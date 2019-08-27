package com.kyleu.projectile.models.auth

import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.NullUtils
import com.mohiva.play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}

final case class UserCredentials(user: SystemUser, remoteAddress: String = NullUtils.str, tags: Map[String, String] = Map.empty) extends Credentials {
  override def id = "user:" + user.id.toString
  override def name = user.username
  override def role = user.role
}

object UserCredentials {
  implicit val jsonEncoder: Encoder[UserCredentials] = deriveEncoder
  implicit val jsonDecoder: Decoder[UserCredentials] = deriveDecoder

  val system = UserCredentials(SystemUser.system, "localhost")

  def fromInsecureRequest(request: UserAwareRequest[AuthEnv, _]) = request.identity.map(u => UserCredentials(u, request.remoteAddress))

  def fromRequest(request: SecuredRequest[AuthEnv, _]) = UserCredentials(request.identity, request.remoteAddress)
}
