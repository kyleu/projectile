package com.kyleu.projectile.controllers.websocket

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.web.util.WebsocketUtils
import com.mohiva.play.silhouette.api.HandlerResult
import io.circe.{Json, JsonObject}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object WebsocketController {
  def errJson(t: String, b: String) = {
    Json.fromJsonObject(JsonObject.apply("status" -> "error".asJson, "top" -> t.asJson, "bottom" -> b.asJson))
  }
}

abstract class WebsocketController[ReqMsg: Decoder, RspMsg: Encoder](name: String) extends AuthController(name) {
  implicit def system: ActorSystem
  implicit def materializer: Materializer

  protected[this] def connectionSupervisor: ActorRef
  protected[this] def onConnect(id: UUID, creds: Credentials, out: ActorRef, sourceAddr: String): Props

  private[this] val formatter = new MessageFrameFormatter[ReqMsg, RspMsg]()

  protected[this] def connectAnonymous() = WebSocket.accept[ReqMsg, RspMsg] { request =>
    implicit val req: Request[AnyContent] = Request(request, AnyContentAsEmpty)
    val connId = UUID.randomUUID()
    WebsocketUtils.actorRef(connId) { out =>
      onConnect(id = connId, creds = Credentials.anonymous, out = out, sourceAddr = request.remoteAddress)
    }
  }(formatter.transformer())

  protected[this] def connectOptAuth() = WebSocket.acceptOrResult[ReqMsg, RspMsg] { request =>
    implicit val req: Request[AnyContent] = Request(request, AnyContentAsEmpty)
    val connId = UUID.randomUUID()
    app.silhouette.UserAwareRequestHandler { ua => Future.successful(HandlerResult(Ok, ua.identity)) }.map {
      case HandlerResult(_, user) => Right(WebsocketUtils.actorRef(connId) { out =>
        val creds = user match {
          case Some(u) => UserCredentials(u, request.remoteAddress)
          case None => Credentials.anonymous
        }
        onConnect(id = connId, creds = creds, out = out, sourceAddr = request.remoteAddress)
      })
    }
  }(formatter.transformer())

  protected[this] def connectAuth() = WebSocket.acceptOrResult[ReqMsg, RspMsg] { request =>
    implicit val req: Request[AnyContent] = Request(request, AnyContentAsEmpty)
    val connId = UUID.randomUUID()
    app.silhouette.SecuredRequestHandler { secured => Future.successful(HandlerResult(Ok, Some(secured.identity))) }.map {
      case HandlerResult(_, Some(user)) => Right(WebsocketUtils.actorRef(connId) { out =>
        onConnect(id = connId, creds = UserCredentials(user, request.remoteAddress), out = out, sourceAddr = request.remoteAddress)
      })
      case HandlerResult(_, None) => Left(Redirect("/").flashing("error" -> "You're not signed in"))
    }
  }(formatter.transformer())
}
