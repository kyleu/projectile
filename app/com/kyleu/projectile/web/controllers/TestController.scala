package com.kyleu.projectile.web.controllers

import com.kyleu.projectile.components.models.config._

import scala.concurrent.Future

@javax.inject.Singleton
class TestController @javax.inject.Inject() () extends ProjectileController {
  def list() = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.test.testlist(projectile, Seq("signin", "signup", "error", "notfound", "page"))))
  }

  def test(key: String, theme: String) = Action.async { implicit request =>
    val menu = NavMenu.test
    val urls = NavUrls()
    val html = NavHtml(headerRightMenu = play.twirl.api.Html(""))
    val user = UserSettings(theme = UiTheme.withValue(theme))
    val breadcrumbs = Seq("test", "a")
    val cfg = UiConfig(projectName = "Test Project", menu = menu, urls = urls, html = html, user = user, breadcrumbs = breadcrumbs)
    val rsp = key match {
      case "signin" => com.kyleu.projectile.components.views.html.auth.signin(cfg = cfg)
      case "signup" => com.kyleu.projectile.components.views.html.auth.signup(cfg = cfg)
      case "error" => com.kyleu.projectile.components.views.html.error.servererror(cfg = cfg)
      case "notfound" => com.kyleu.projectile.components.views.html.error.notfound(cfg = cfg)
      case "page" => com.kyleu.projectile.web.views.html.test.components(title = "Testbed", cfg = cfg)
      case _ => throw new IllegalStateException(s"Unhandled test [$key]")
    }
    Future.successful(Ok(rsp))
  }
}
