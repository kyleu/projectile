package com.kyleu.projectile.web.controllers

import com.kyleu.projectile.models.config._

import scala.concurrent.Future

@javax.inject.Singleton
class TestController @javax.inject.Inject() () extends ProjectileController {
  def list() = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.test.testlist(projectile, Seq("signin", "signup", "error", "notfound", "page"))))
  }

  def test(key: String, theme: String) = Action.async { implicit request =>
    val menu = Nil
    val urls = NavUrls()
    val html = NavHtml()
    val user = UserSettings(name = "Guest", theme = theme)
    val breadcrumbs = Seq(BreadcrumbEntry("section1", "Section 1", Some("one")), BreadcrumbEntry("test", "Test", Some("t")), BreadcrumbEntry("a", "Section A", Some("a")))
    val cfg = UiConfig(projectName = "Test Project", menu = menu, urls = urls, html = html, user = user, breadcrumbs = breadcrumbs)
    val rsp = key match {
      case "signin" => com.kyleu.projectile.components.views.html.auth.signin(username = "", allowRegistration = true, cfg = cfg)
      case "signup" => com.kyleu.projectile.components.views.html.auth.signup(username = "", email = "", cfg = cfg)
      case "error" => com.kyleu.projectile.components.views.html.error.servererror(cfg = cfg)
      case "notfound" => com.kyleu.projectile.components.views.html.error.notfound(cfg = cfg)
      case "page" => com.kyleu.projectile.web.views.html.test.components(title = "Testbed", cfg = cfg)
      case _ => throw new IllegalStateException(s"Unhandled test [$key]")
    }
    Future.successful(Ok(rsp))
  }
}
