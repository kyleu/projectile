package com.kyleu.projectile.controllers.websocket

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import com.kyleu.projectile.controllers.BaseController
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.services.connection.EchoConnectionService
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.web.util.WebsocketUtils
import play.api.mvc._

abstract class WebsocketController[Req: Decoder, Rsp: Encoder](name: String) extends BaseController(name) {
  implicit def system: ActorSystem
  implicit def materializer: Materializer

  def connectionSupervisor: ActorRef
  def onRequest(r: Req): Unit

  private[this] val formatter = new MessageFrameFormatter[Req, Rsp]()

  protected def connect(credsFor: RequestHeader => Credentials) = WebSocket.accept[Req, Rsp] { request =>
    implicit val req: Request[AnyContent] = Request(request, AnyContentAsEmpty)
    val connId = UUID.randomUUID()
    WebsocketUtils.actorRef(connId) { out =>
      EchoConnectionService.props(
        id = Some(connId),
        connSupervisor = connectionSupervisor,
        creds = credsFor(request),
        out = out,
        sourceAddr = request.remoteAddress
      )
    }
  }(formatter.transformer())
}
