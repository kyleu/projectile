package com.kyleu.projectile.controllers.websocket

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.BinarySerializers.Pickler
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.models.web.WebsocketUtils
import com.mohiva.play.silhouette.api.HandlerResult
import io.circe.{Json, JsonObject}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object WebsocketController {
  def errJson(t: String, b: String) = {
    Json.fromJsonObject(JsonObject.apply("status" -> "error".asJson, "top" -> t.asJson, "bottom" -> b.asJson))
  }
}

abstract class WebsocketController[ClientMsg: Decoder: Pickler, ServerMsg: Encoder: Pickler](name: String)(
    implicit
    ec: ExecutionContext
) extends AuthController(name) {
  implicit def system: ActorSystem
  implicit def materializer: Materializer

  protected[this] def onConnect(connectionId: UUID, request: RequestHeader, creds: Credentials, out: ActorRef): Props

  private[this] val formatter = new MessageFrameFormatter[ClientMsg, ServerMsg]()

  def connectAnonymous(binary: Boolean) = WebSocket.accept[ClientMsg, ServerMsg] { request =>
    val connectionId = UUID.randomUUID()
    WebsocketUtils.actorRef(connectionId) { out =>
      onConnect(connectionId = connectionId, creds = Credentials.anonymous, out = out, request = request)
    }
  }(formatter.transformer(binary))

  def connect(binary: Boolean) = WebSocket.acceptOrResult[ClientMsg, ServerMsg] { request =>
    val connectionId = UUID.randomUUID()
    implicit val req: Request[AnyContent] = Request(request, AnyContentAsEmpty)
    app.silhouette.UserAwareRequestHandler { ua => Future.successful(HandlerResult(Ok, ua.identity)) }.map {
      case HandlerResult(_, user) => Right(WebsocketUtils.actorRef(connectionId) { out =>
        val creds = user match {
          case Some(u) => UserCredentials(u, request.remoteAddress)
          case None => Credentials.anonymous
        }
        onConnect(connectionId = connectionId, request = request, creds = creds, out = out)
      })
    }
  }(formatter.transformer(binary))
}
