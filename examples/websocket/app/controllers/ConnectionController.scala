package controllers

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import com.kyleu.projectile.controllers.websocket.WebsocketController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.services.Credentials
import models.message.{ClientMessage, ServerMessage}
import play.api.mvc.RequestHeader
import services.actor.ParticipantConnectionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionController @javax.inject.Inject() (
    override val app: Application,
    @javax.inject.Named("connection-supervisor") val connectionSupervisor: ActorRef,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends WebsocketController[ClientMessage, ServerMessage]("home") {

  def test() = withoutSession("home") { implicit request => implicit td =>
    val creds = UserCredentials.fromInsecureRequest(request).getOrElse(Credentials.anonymous)
    val r = Ok(views.html.connect(request.identity, app.config.debug))
    Future.successful(r)
  }

  override protected[this] def onConnect(id: UUID, request: RequestHeader, creds: Credentials, out: ActorRef) = {
    ParticipantConnectionService.props(id, None /* TODO */, connectionSupervisor, creds, out, request.remoteAddress)
  }
}
