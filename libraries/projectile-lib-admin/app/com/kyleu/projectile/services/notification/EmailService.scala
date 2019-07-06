package com.kyleu.projectile.services.notification

import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import play.api.libs.mailer.{Email, MailerClient}

import scala.util.control.NonFatal

@javax.inject.Singleton
class EmailService @javax.inject.Inject() (client: MailerClient) extends Logging {
  def send(email: Email)(implicit td: TraceData) = try {
    Right(client.send(email))
  } catch {
    case NonFatal(x) =>
      log.error("Error sending email", x)
      Left(x)
  }
}
