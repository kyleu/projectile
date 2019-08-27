package controllers

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import com.kyleu.projectile.controllers.websocket.WebSocketController
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.services.websocket.EchoConnectionService
import io.circe.Json
import play.api.mvc.RequestHeader
import com.kyleu.projectile.util.BinarySerializers._

import scala.concurrent.ExecutionContext.Implicits.global

@javax.inject.Singleton
class ConnectionController @javax.inject.Inject() (
    override val app: Application,
    @javax.inject.Named("connection-supervisor") val connectionSupervisor: ActorRef,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends WebSocketController[Json, Json]("home") {
  override protected[this] def onConnect(id: UUID, request: RequestHeader, creds: Credentials, out: ActorRef) = {
    EchoConnectionService.props(id, connectionSupervisor, creds, out, request.remoteAddress)
  }
}
