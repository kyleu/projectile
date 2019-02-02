package com.kyleu.projectile.models.supervisor

import java.util.UUID

import akka.actor.ActorRef
import com.kyleu.projectile.services.Credentials
import io.circe.Json

sealed trait InternalMessage

object InternalMessage {
  // Request
  final case class ConnectionStarted(creds: Credentials, channel: String, id: UUID, userId: String, username: String, conn: ActorRef) extends InternalMessage
  final case class ConnectionStopped(id: UUID) extends InternalMessage

  case object GetSystemStatus extends InternalMessage
  final case class ConnectionTraceRequest(id: UUID) extends InternalMessage
  final case class ClientTraceRequest(id: UUID) extends InternalMessage
  final case class SessionTraceRequest(id: UUID) extends InternalMessage

  // Response
  final case class ConnectionStatus(connections: Seq[ConnectionDescription]) extends InternalMessage
  final case class SessionStatus(games: Seq[SessionDescription]) extends InternalMessage
  final case class ConnectionTraceResponse(id: UUID, userId: String, username: String) extends InternalMessage
  final case class ClientTraceResponse(id: UUID, data: Json) extends InternalMessage
}
