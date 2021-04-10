package com.kyleu.projectile.models.auth.microsoft

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, OAuth2Info, SocialProfileParser}
import play.api.libs.json.JsValue

import scala.concurrent.Future

class MicrosoftProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info] {
  override def parse(json: JsValue, authInfo: OAuth2Info): Future[CommonSocialProfile] = Future.successful {
    CommonSocialProfile(
      loginInfo = LoginInfo(BaseMicrosoftProvider.ID, (json \ "id").as[String]),
      firstName = (json \ "givenName").asOpt[String],
      lastName = (json \ "surname").asOpt[String],
      fullName = (json \ "userPrincipalName").asOpt[String],
      avatarURL = None,
      email = Some((json \ "mail").as[String])
    )
  }
}
