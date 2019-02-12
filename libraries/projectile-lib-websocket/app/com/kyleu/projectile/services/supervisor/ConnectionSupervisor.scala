package com.kyleu.projectile.services.supervisor

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import com.kyleu.projectile.models.supervisor.InternalMessage._
import com.kyleu.projectile.models.supervisor.{ConnectionDescription, InternalMessage}
import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.util.{DateUtils, Logging}

object ConnectionSupervisor {
  protected var initialized = false

  final case class Broadcast(msg: String)
  final case class ConnectionRecord(id: UUID, userId: String, username: String, actorRef: ActorRef, started: LocalDateTime) {
    val desc = ConnectionDescription(id, userId, username, "connection", started)
  }

  def props(err: (String, String) => AnyRef) = Props(new ConnectionSupervisor(err))
}

class ConnectionSupervisor(err: (String, String) => AnyRef) extends Actor with Logging {
  private[this] implicit val td: TraceData = TraceData.noop

  private[this] val connections = collection.mutable.HashMap.empty[UUID, ConnectionSupervisor.ConnectionRecord]
  private[this] def connectionById(id: UUID) = connections.get(id)

  override def preStart() = {
    if (ConnectionSupervisor.initialized) {
      log.warn("Only one ConnectionSupervisor can be started")
    }
    log.debug(s"Connection Supervisor started")
    ConnectionSupervisor.initialized = true
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case x: IllegalStateException =>
      val msg = s"Connection Actor encountered [${x.getClass.getSimpleName}]: ${x.getMessage}"
      log.error(msg, x)
      self.tell(err("connection-exception", msg), self)
      SupervisorStrategy.Stop
    case x: Exception =>
      val msg = s"Connection Actor encountered [${x.getClass.getSimpleName}]: ${x.getMessage}"
      log.error(msg, x)
      SupervisorStrategy.Stop
  }

  override def receive = {
    case ss: ConnectionStarted => handleConnectionStarted(ss.id, ss.creds.id, ss.creds.name, ss.conn)
    case ss: ConnectionStopped => handleConnectionStopped(ss.id)

    case GetSystemStatus => handleGetSystemStatus()
    case sst: ConnectionTraceRequest => handleSendConnectionTrace(sst)
    case sct: ClientTraceRequest => handleSendClientTrace(sct)

    case b: ConnectionSupervisor.Broadcast => connections.foreach(_._2.actorRef.tell(b.msg, self))

    case im: InternalMessage => log.warn(s"Unhandled connection supervisor internal message [${im.getClass.getSimpleName}]")
    case x => log.warn(s"ConnectionSupervisor encountered unknown message: ${x.toString}")
  }

  override def postStop() = {
    ConnectionSupervisor.initialized = false
  }

  private[this] def handleGetSystemStatus() = sender().tell(ConnectionStatus(connections.map(_._2.desc).toSeq.sortBy(_.username)), self)

  private[this] def handleSendConnectionTrace(ct: ConnectionTraceRequest) = connectionById(ct.id) match {
    case Some(c) => c.actorRef forward ct
    case None => sender().tell(err("Unknown connection", ct.id.toString), self)
  }

  private[this] def handleSendClientTrace(ct: ClientTraceRequest) = connectionById(ct.id) match {
    case Some(c) => c.actorRef forward ct
    case None => sender().tell(err("Unknown connection", ct.id.toString), self)
  }

  protected[this] def handleConnectionStarted(id: UUID, userId: String, name: String, connection: ActorRef) = {
    log.debug(s"Connection [$id] registered to user [$userId] with path [${connection.path}]")
    connections(id) = ConnectionSupervisor.ConnectionRecord(id, userId, name, connection, DateUtils.now)
  }

  protected[this] def handleConnectionStopped(id: UUID) = {
    connections.remove(id).foreach(sock => log.debug(s"Connection [$id] [${sock.actorRef.path}] removed from Audit supervisor"))
  }
}
