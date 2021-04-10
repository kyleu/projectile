package com.kyleu.projectile.models.auth.microsoft

import com.mohiva.play.silhouette.api.util.{ExtractableRequest, HTTPLayer}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, OAuth2Info, OAuth2Settings, SocialStateHandler}
import play.api.mvc.Result

import scala.concurrent.Future

class MicrosoftProvider(protected val httpLayer: HTTPLayer, protected val stateHandler: SocialStateHandler, val settings: OAuth2Settings)
  extends BaseMicrosoftProvider with CommonSocialProfileBuilder {

  type Self = MicrosoftProvider
  val profileParser = new MicrosoftProfileParser()

  def withSettings(f: (Settings) => Settings) = new MicrosoftProvider(httpLayer, stateHandler, f(settings))

  override def authenticate[B]()(implicit request: ExtractableRequest[B]): Future[Either[Result, OAuth2Info]] = {
    super.authenticate()(request)
  }
}
