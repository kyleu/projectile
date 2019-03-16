package services.actor

import java.util.UUID

import akka.actor.{ActorRef, Props}
import com.kyleu.projectile.models.connection.ConnectionMessage
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.services.connection.ConnectionService
import io.circe.Json
import models.message.{ClientMessage, ServerMessage}
import util.Version

object ParticipantConnectionService {
  def props(id: UUID, u: Option[SystemUser], connSupervisor: ActorRef, creds: Credentials, out: ActorRef, sourceAddr: String) = {
    Props(new ParticipantConnectionService(id = id, u = u, connSupervisor = connSupervisor, creds = creds, out = out, sourceAddr = sourceAddr))
  }
}

class ParticipantConnectionService(
    override val id: UUID, u: Option[SystemUser], connSupervisor: ActorRef,
    override val creds: Credentials, override val out: ActorRef, override val sourceAddr: String
) extends ConnectionService[ClientMessage, ServerMessage](id, connSupervisor, "participant", creds, out, sourceAddr) {
  override def onConnect() = out.tell(ServerMessage.VersionResponse(version = Version.version), self)

  override def status() = ConnectionMessage.ConnectionTraceResponse(id, Json.fromString("TODO"))

  override def onMessage = {
    case ClientMessage.Ping(ts) => send(ServerMessage.Pong(ts, System.currentTimeMillis))
    case x: ServerMessage => send(x)
  }

  private[this] def send(r: ServerMessage) = out ! r
}
