package com.kyleu.projectile.services.connection

import java.util.UUID

import akka.actor.{Actor, ActorRef, Timers}
import com.kyleu.projectile.models.supervisor.InternalMessage
import com.kyleu.projectile.models.supervisor.InternalMessage._
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData

abstract class ConnectionService[Req, Rsp](
    val id: UUID, connSupervisor: ActorRef, protected val creds: Credentials,
    protected val out: ActorRef, protected val sourceAddr: String
) extends Logging with Actor with Timers {
  protected[this] implicit val td: TraceData = TraceData.noop

  override def preStart() = {
    log.info(s"Starting player connection for user [${creds.id}: ${creds.name}].")
    val msg = ConnectionStarted(creds = creds, channel = "player", id = id, userId = creds.id, username = creds.name, conn = self)
    connSupervisor.tell(msg, self)
    onConnect()
  }

  def onConnect(): Unit
  def onRequest(request: Req): Unit

  final override def receive = {
    // Requests
    case r if r.isInstanceOf[Req] => onRequest(r.asInstanceOf[Req])

    // Responses
    case r if r.isInstanceOf[Rsp] => out.tell(r.asInstanceOf[Rsp], self)

    // Tracing
    case _: ConnectionTraceRequest => sender().tell(ConnectionTraceResponse(id = id, userId = creds.id, username = creds.name), self)

    // Unhandled
    case im: InternalMessage => throw new IllegalArgumentException(s"Unhandled internal connection message [${im.getClass.getSimpleName}].")
    case x => throw new IllegalArgumentException(s"Unhandled connection message with invalid class [${x.getClass.getSimpleName}].")
  }

  override def postStop() = {
    connSupervisor.tell(ConnectionStopped(id), self)
  }
}
