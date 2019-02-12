package com.kyleu.projectile.controllers.connection

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.supervisor.InternalMessage._
import com.kyleu.projectile.services.supervisor.ConnectionSupervisor

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@javax.inject.Singleton
class ConnectionActivityController @javax.inject.Inject() (
    override val app: Application,
    @javax.inject.Named("connection-supervisor") val connSupervisor: ActorRef
) extends AuthController("admin.activity") {

  def activityIndex = withSession("activity.index", admin = true) { implicit request => implicit td =>
    Future.successful(Ok(com.kyleu.projectile.views.html.activity.activityIndex(request.identity)))
  }

  def broadcast(msg: Option[String]) = withSession("activity.broadcast", admin = true) { implicit request => implicit td =>
    msg.map(_.trim) match {
      case None => throw new IllegalStateException("Must provide \"msg\" parameter")
      case Some(message) if message.isEmpty => throw new IllegalStateException("Empty message")
      case Some(message) =>
        connSupervisor ! ConnectionSupervisor.Broadcast(message)
        val status = s"Message [$message] broadcast successfully"
        Future.successful(Redirect(com.kyleu.projectile.controllers.activity.routes.ActivityController.activityIndex()).flashing("success" -> status))
    }
  }

  def connectionList = withSession("activity.connection.list", admin = true) { implicit request => implicit td =>
    ask(connSupervisor, GetSystemStatus)(20.seconds).mapTo[ConnectionStatus].map { status =>
      Ok(com.kyleu.projectile.views.html.activity.connectionList(request.identity, status.connections))
    }
  }

  def connectionDetail(id: UUID) = withSession("activity.connection.detail", admin = true) { implicit request => implicit td =>
    ask(connSupervisor, ConnectionTraceRequest(id))(20.seconds).mapTo[ConnectionTraceResponse].map { c =>
      Ok(com.kyleu.projectile.views.html.activity.connectionDetail(request.identity, c))
    }
  }

  def clientDetail(id: UUID) = withSession("activity.client.detail", admin = true) { implicit request => implicit td =>
    ask(connSupervisor, ClientTraceRequest(id))(20.seconds).mapTo[ClientTraceResponse].map { c =>
      Ok(com.kyleu.projectile.views.html.activity.clientDetail(request.identity, c))
    }
  }
}
