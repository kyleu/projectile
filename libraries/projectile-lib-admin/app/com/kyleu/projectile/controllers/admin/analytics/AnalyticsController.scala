package com.kyleu.projectile.controllers.admin.analytics

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.config.UiConfig
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService

import scala.concurrent.{ExecutionContext, Future}

object AnalyticsController {
  private[this] var uaOpt: Option[String] = None
  def init(ua: String) = uaOpt = Some(ua)

  def injectHtml(cfg: UiConfig, params: (String, String)*) = {
    uaOpt.map(ua => com.kyleu.projectile.views.html.admin.analytics.google(ua, cfg, params))
  }
}

@javax.inject.Singleton
class AnalyticsController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("analytics") {
  ApplicationFeature.enable(ApplicationFeature.Analytics)
  PermissionService.registerModel("tools", "Analytics", "Analytics", Some(InternalIcons.analytics), "view")
  val desc = "Sends you to the analytics dashboard for this application"
  SystemMenu.addToolMenu(ApplicationFeature.Analytics.value, "Analytics", Some(desc), routes.AnalyticsController.redir(), InternalIcons.analytics)

  val googleUa = app.config.metrics.analyticsGoogleUa
  googleUa.foreach(AnalyticsController.init)

  def redir = withSession("redir", ("tools", "Analytics", "view")) { _ => _ =>
    googleUa match {
      case Some(ua) => Future.successful(Redirect("https://analytics.google.com/analytics/web"))
      case None => Future.successful(Ok("No analytics key has been defined. Set [metrics.analytics.google.ua] in your config file to proceed"))
    }
  }
}
