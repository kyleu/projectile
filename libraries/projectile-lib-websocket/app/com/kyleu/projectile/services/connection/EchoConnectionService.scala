package com.kyleu.projectile.services.connection

import java.util.UUID

import akka.actor.{ActorRef, Props}
import com.kyleu.projectile.services.Credentials
import io.circe.Json

object EchoConnectionService {
  def props[Req, Rsp](
    id: Option[UUID], connSupervisor: ActorRef, creds: Credentials, out: ActorRef, sourceAddr: String
  ) = {
    Props(new EchoConnectionService(id.getOrElse(UUID.randomUUID), connSupervisor, creds, out, sourceAddr))
  }
}

class EchoConnectionService(
    override val id: UUID, connSupervisor: ActorRef, protected override val creds: Credentials,
    protected override val out: ActorRef, protected override val sourceAddr: String
) extends ConnectionService[Json, Json](id, connSupervisor, creds, out, sourceAddr) {

  override def onConnect() = out.tell(Json.fromString("connected"), self)

  def onRequest(request: Json) = out.tell(request, self)
}
