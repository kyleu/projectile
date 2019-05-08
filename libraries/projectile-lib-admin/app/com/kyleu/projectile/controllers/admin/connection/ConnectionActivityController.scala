package com.kyleu.projectile.controllers.admin.connection

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.connection.ConnectionMessage._
import com.kyleu.projectile.services.connection.ConnectionSupervisor

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@javax.inject.Singleton
class ConnectionActivityController @javax.inject.Inject() (
    override val app: Application,
    @javax.inject.Named("connection-supervisor") val connSupervisor: ActorRef
)(implicit ec: ExecutionContext) extends AuthController("admin.activity") {
  def connectionList = withSession("activity.connection.list", admin = true) { implicit request => implicit td =>
    ask(connSupervisor, GetConnectionStatus)(20.seconds).mapTo[ConnectionStatus].map { status =>
      Ok(com.kyleu.projectile.views.html.admin.activity.connectionList(app.cfg(u = Some(request.identity), admin = true), status.connections))
    }
  }

  def connectionDetail(id: UUID) = withSession("activity.connection.detail", admin = true) { implicit request => implicit td =>
    ask(connSupervisor, ConnectionTraceRequest(id))(20.seconds).mapTo[ConnectionTraceResponse].map { c =>
      Ok(com.kyleu.projectile.views.html.admin.activity.connectionDetail(app.cfg(u = Some(request.identity), admin = true), c))
    }
  }

  def broadcast(msg: Option[String]) = withSession("activity.broadcast", admin = true) { implicit request => implicit td =>
    msg.map(_.trim) match {
      case None => throw new IllegalStateException("Must provide \"msg\" parameter")
      case Some(message) if message.isEmpty => throw new IllegalStateException("Empty message")
      case Some(message) =>
        connSupervisor ! ConnectionSupervisor.Broadcast(message)
        val status = s"Message [$message] broadcast successfully"
        val call = com.kyleu.projectile.controllers.admin.connection.routes.ConnectionActivityController.connectionList()
        Future.successful(Redirect(call).flashing("success" -> status))
    }
  }
}
