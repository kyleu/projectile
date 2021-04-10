package com.kyleu.projectile.models.auth.microsoft

import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers._
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.Future

object BaseMicrosoftProvider {
  val ID = "microsoft"
  val specifiedProfileError = "[Silhouette][%s] Error retrieving profile information. Error code: %s, message: %s"
}

trait BaseMicrosoftProvider extends OAuth2Provider {
  override type Content = JsValue
  override val id: String = BaseMicrosoftProvider.ID
  override protected val urls = Map("api" -> settings.apiURL.getOrElse("https://graph.microsoft.com/v1.0/me"))

  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    val u = urls("api")
    val call = httpLayer.url(u).addHttpHeaders("Authorization" -> ("Bearer " + authInfo.accessToken))
    call.get().flatMap { response =>
      val json = response.json
      (json \ "error").asOpt[JsObject] match {
        case Some(error) =>
          val errorCode = (error \ "code")
          val errorMsg = (error \ "message")

          throw new ProfileRetrievalException(BaseMicrosoftProvider.specifiedProfileError.format(id, errorCode, errorMsg))
        case _ => profileParser.parse(json, authInfo)
      }
    }
  }
}
