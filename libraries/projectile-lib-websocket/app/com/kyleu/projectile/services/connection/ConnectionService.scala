package com.kyleu.projectile.services.connection

import java.util.UUID

import akka.actor.{Actor, ActorRef, Timers}
import com.kyleu.projectile.models.connection.ConnectionMessage._
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.JsonSerializers.Json
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData

abstract class ConnectionService[Req, Rsp](
    val id: UUID, connSupervisor: ActorRef, protected val channel: String, protected val creds: Credentials,
    protected val out: ActorRef, protected val sourceAddr: String
) extends Logging with Actor with Timers {
  protected[this] implicit val td: TraceData = TraceData.noop

  override def preStart() = {
    log.info(s"Starting [$channel] connection for user [${creds.id}: ${creds.name}]")
    val msg = ConnectionStarted(creds = creds, channel = channel, id = id, userId = creds.id, username = creds.name, conn = self)
    connSupervisor.tell(msg, self)
    onConnect()
  }

  def onConnect(): Unit
  def onMessage: PartialFunction[Any, Unit]
  def status(): ConnectionTraceResponse

  final override def receive = {
    case _: ConnectionTraceRequest => sender().tell(status(), self)
    case _: ClientTraceRequest => sendClientTrace()
    case ct: ClientTraceResponse => returnClientTrace(ct.id, ct.data)

    case x if onMessage.isDefinedAt(x) => onMessage(x)
    case x => log.error(s"Unhandled connection service message [$x]")
  }

  override def postStop() = {
    connSupervisor.tell(ConnectionStopped(id), self)
  }

  // Trace
  private[this] var pendingClientTrace: Option[ActorRef] = None

  protected[this] def sendClientTrace() = {
    pendingClientTrace = Some(sender())
    out.tell("trace", self)
  }

  protected[this] def returnClientTrace(id: UUID, data: Json) = pendingClientTrace match {
    case Some(a) =>
      pendingClientTrace = None
      a.tell(ClientTraceResponse(id, data), self)
    case None => throw new IllegalStateException("No pending client trace")
  }
}
