package com.kyleu.projectile.controllers.admin.encrypt

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.module.{Application, ApplicationFeatures}
import com.kyleu.projectile.models.web.ControllerUtils
import com.kyleu.projectile.util.EncryptionUtils

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class EncryptionController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("encryption") {
  ApplicationFeatures.enable("encryption")

  def form = withSession("list", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "encryption")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.encrypt.encryption(cfg)))
  }

  def post() = withSession("list", admin = true) { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    val action = form.get("action")
    val (unenc, enc) = action match {
      case Some("encrypt") =>
        val u = form.getOrElse("unenc", throw new IllegalStateException("Must provide [unenc] value when action is [encrypt]."))
        u -> EncryptionUtils.encrypt(u)
      case Some("decrypt") =>
        val e = form.getOrElse("enc", throw new IllegalStateException("Must provide [enc] value when action is [decrypt]."))
        EncryptionUtils.decrypt(e) -> e
      case _ => throw new IllegalStateException("Must provide [action] value of \"encrypt\" or \"decrypt\".")
    }

    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "encryption")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.encrypt.encryption(cfg, unenc, enc)))
  }
}
