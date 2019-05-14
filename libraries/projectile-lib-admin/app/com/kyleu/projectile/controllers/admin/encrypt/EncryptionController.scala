package com.kyleu.projectile.controllers.admin.encrypt

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.web.ControllerUtils
import com.kyleu.projectile.util.EncryptionUtils

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class EncryptionController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("encryption") {
  def form = withSession("list", admin = true) { implicit request => implicit td =>
    val cfg = app.cfg(Some(request.identity), admin = true, "system", "encryption")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.encrypt.encryption(request.identity, cfg)))
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

    val cfg = app.cfg(Some(request.identity), admin = true, "system", "encryption")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.encrypt.encryption(request.identity, cfg, unenc, enc)))
  }
}
