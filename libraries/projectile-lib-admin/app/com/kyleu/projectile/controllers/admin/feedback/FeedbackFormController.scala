package com.kyleu.projectile.controllers.admin.feedback

import java.util.UUID

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.feedback.routes.FeedbackController
import com.kyleu.projectile.models.feedback.Feedback
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.{ControllerUtils, InternalIcons}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.feedback.FeedbackService
import com.kyleu.projectile.services.notification.EmailService
import com.kyleu.projectile.util.DateUtils
import play.api.libs.mailer.Email
import com.kyleu.projectile.services.Credentials

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class FeedbackFormController @javax.inject.Inject() (
    override val app: Application, emailSvc: EmailService, feedbackSvc: FeedbackService
)(implicit ec: ExecutionContext) extends AuthController("feedback") {
  ApplicationFeature.enable(ApplicationFeature.Feedback)
  app.errors.checkTable("feedback")

  val desc = "Allows you to submit feedback to the authors of this application"
  SystemMenu.addToolMenu(ApplicationFeature.Feedback.value, "Feedback", Some(desc), FeedbackController.list(), InternalIcons.feedback)
  PermissionService.registerModel("tools", "Feedback", "Feedback", Some(InternalIcons.feedback), "view")

  def form = withoutSession("form") { implicit request => implicit td =>
    val cfg = app.cfg(u = request.identity, "Send Feedback")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.feedback.form(cfg)))
  }

  def post() = withoutSession("post") { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    val msg = form.getOrElse("msg", throw new IllegalStateException("Form field [msg] is required"))

    val sender = request.identity.map(u => u.id -> u.email).getOrElse(UUID.fromString("00000000-0000-0000-0000-000000000000") -> "anonymous")

    val from = "kyle@kyleu.com"
    val to = "kyle@kyleu.com"
    val dt = DateUtils.niceDateTime(DateUtils.now)
    val author = s"${sender._2} [${sender._1}]"

    val email = Email(
      subject = s"Feedback submitted from [${sender._2}]", from = from, to = Seq(to),
      bodyText = Some(s"From: $author\nSent: $dt\n$msg"),
      bodyHtml = Some(s"<body><h3>From: $author</h3>\n<h4>Sent: $dt</h4>\n<pre>$msg</pre></body>")
    )
    emailSvc.send(email) match {
      case Right(ok) => log.info(s"Email sent successfully to [$to]")
      case Left(x) => log.error("Error sending feedback email", x)
    }

    feedbackSvc.insert(Credentials.noop, Feedback.empty(text = msg, authorId = sender._1, authorEmail = sender._2, status = "OK")).map { _ =>
      Redirect("/").flashing("success" -> "Thanks for submitting your feedback, we'll respond soon")
    }
  }
}
