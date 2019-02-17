package com.kyleu.projectile.models.connection

import java.util.UUID

import akka.actor.ActorRef
import com.kyleu.projectile.services.Credentials
import io.circe.Json

sealed trait ConnectionMessage

object ConnectionMessage {
  // Request
  final case class ConnectionStarted(creds: Credentials, channel: String, id: UUID, userId: String, username: String, conn: ActorRef) extends ConnectionMessage
  final case class ConnectionStopped(id: UUID) extends ConnectionMessage

  case object GetConnectionStatus extends ConnectionMessage
  final case class ConnectionTraceRequest(id: UUID) extends ConnectionMessage

  // Response
  final case class ConnectionStatus(connections: Seq[ConnectionDescription]) extends ConnectionMessage
  final case class ConnectionTraceResponse(id: UUID, data: Json) extends ConnectionMessage
}
