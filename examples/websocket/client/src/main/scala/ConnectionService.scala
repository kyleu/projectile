import com.kyleu.projectile.services.socket.{EventHandler, SocketConnection}
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.{ExceptionHandler, Logging}
import models.message.ClientMessage.Ping
import models.message.ServerMessage.{Pong, VersionResponse}
import models.message.{ClientMessage, ServerMessage}
import org.scalajs.dom.raw.Event
import util.Version

import scala.scalajs.js.timers.setInterval

class ConnectionService(participantId: String, binary: Boolean) extends EventHandler[ServerMessage] {
  private[this] val conn: SocketConnection[ClientMessage, ServerMessage] = new SocketConnection(Version.projectId, "/connect", binary, this)

  private[this] var runningPingSchedule = false

  def open() = {
    ExceptionHandler.install()
    Logging.info(s"${Version.projectName} v${Version.version} started")
  }

  override def onConnect() = {
    Logging.debug("Connection opened")
    if (!runningPingSchedule) {
      setInterval(10000)(conn.sendMessage(Ping(System.currentTimeMillis)))
      runningPingSchedule = true
    }
  }

  override def onMessage(msg: ServerMessage) = msg match {
    case VersionResponse(v) => onVersion(v)
    case Pong(ts, srv) => updateLatency(ts, srv)
    case _ => Logging.info(s"Unhandled message: ${msg.asJson.spaces2}")
  }

  override def onError(err: Event) = Logging.info(s"Error: $err")

  override def onClose() = {
    Logging.debug("Connection closed")
  }

  private[this] def onVersion(v: String) = if (v != Version.version) {
    Logging.warn(s"Server is version [$v] while client code is version [${Version.version}]")
  }

  private[this] def updateLatency(ts: Long, srv: Long) = {
    val l = System.currentTimeMillis - ts
    Logging.info(s"Latency: ${l}ms")
  }
}
