package com.kyleu.projectile.controllers.admin.encrypt

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.encrypt.routes.EncryptionController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Encryption.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.{ControllerUtils, InternalIcons}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.EncryptionUtils

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class EncryptionController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("encryption") {
  ApplicationFeature.enable(ApplicationFeature.Encryption)
  PermissionService.registerModel("tools", "Encryption", "Encryption", Some(InternalIcons.encryption), "form", "encrypt", "decrypt")
  val desc = "Allows you to encrypt and decrypt strings using the system keys"
  SystemMenu.addToolMenu(value, "Encryption", Some(desc), EncryptionController.form(), InternalIcons.encryption)

  def form = withSession("form", ("tools", "Encryption", "form")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "encryption")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.encrypt.encryption(cfg)))
  }

  def post() = withSession("post", ("audit", "Connection", "encrypt"), ("tools", "Connection", "decrypt")) { implicit request => implicit td =>
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

    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "encryption")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.encrypt.encryption(cfg, unenc, enc)))
  }
}
