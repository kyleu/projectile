package com.kyleu.projectile.controllers.admin.websocket

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.websocket.routes.ConnectionActivityController
import com.kyleu.projectile.models.websocket.ConnectionMessage._
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.websocket.ConnectionSupervisor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ConnectionActivityController @javax.inject.Inject() (
    override val app: Application,
    @javax.inject.Named("connection-supervisor") val connSupervisor: ActorRef
)(implicit ec: ExecutionContext) extends AuthController("admin.websocket") {
  ApplicationFeature.enable(ApplicationFeature.Connection)
  PermissionService.registerModel("tools", "WebSocket", "WebSocket", Some(InternalIcons.connection), "view", "broadcast")
  SystemMenu.addToolMenu(
    key = "websocket",
    title = "WebSockets",
    desc = Some("Lists and manages WebSocket connections"),
    call = ConnectionActivityController.connectionList(),
    icon = InternalIcons.connection,
    ("tools", "WebSocket", "view")
  )

  def connectionList = withSession("list", ("tools", "WebSocket", "view")) { implicit request => implicit td =>
    ask(connSupervisor, GetConnectionStatus)(20.seconds).mapTo[ConnectionStatus].map { status =>
      val cfg = app.cfg(u = Some(request.identity), "system", "tools", "websocket")
      Ok(com.kyleu.projectile.views.html.admin.websocket.connectionList(cfg, status.connections))
    }
  }

  def connectionDetail(id: UUID) = withSession("detail", ("tools", "WebSocket", "view")) { implicit request => implicit td =>
    ask(connSupervisor, ConnectionTraceRequest(id))(20.seconds).mapTo[ConnectionTraceResponse].map { c =>
      val cfg = app.cfg(u = Some(request.identity), "system", "tools", "websocket", id.toString)
      Ok(com.kyleu.projectile.views.html.admin.websocket.connectionDetail(cfg, c))
    }
  }

  def broadcast(msg: Option[String]) = withSession("broadcast", ("tools", "WebSocket", "broadcast")) { _ => _ =>
    msg.map(_.trim) match {
      case None => throw new IllegalStateException("Must provide \"msg\" parameter")
      case Some(message) if message.isEmpty => throw new IllegalStateException("Empty message")
      case Some(message) =>
        connSupervisor ! ConnectionSupervisor.Broadcast(message)
        val status = s"Message [$message] broadcast successfully"
        val call = com.kyleu.projectile.controllers.admin.websocket.routes.ConnectionActivityController.connectionList()
        Future.successful(Redirect(call).flashing("success" -> status))
    }
  }
}
