package com.kyleu.projectile.services.websocket

import java.util.UUID

import akka.actor.{ActorRef, Props}
import com.kyleu.projectile.models.websocket.ConnectionMessage._
import com.kyleu.projectile.util.Credentials
import io.circe.Json

object EchoConnectionService {
  def props[Req, Rsp](id: UUID, connSupervisor: ActorRef, creds: Credentials, out: ActorRef, sourceAddr: String) = {
    Props(new EchoConnectionService(id, connSupervisor, creds, out, sourceAddr))
  }
}

class EchoConnectionService(
    override val id: UUID, connSupervisor: ActorRef, protected override val creds: Credentials,
    protected override val out: ActorRef, protected override val sourceAddr: String
) extends ConnectionService[Json, Json](id, connSupervisor, "echo", creds, out, sourceAddr) {

  override def onConnect() = out.tell(Json.fromString("connected"), self)

  override def status() = ConnectionTraceResponse(id, Json.fromString("Echo Service"))

  override def onMessage = {
    case j: Json => out.tell(j, self)
    case x => throw new IllegalStateException(s"Invalid echo message [$x], json expected")
  }
}
