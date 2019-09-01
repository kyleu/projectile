import com.kyleu.projectile.services.socket.{EventHandler, SocketConnection}
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.BinarySerializers._
import com.kyleu.projectile.util.Logging
import org.scalajs.dom
import org.scalajs.dom.raw.Event

class ConnectionService(binary: Boolean) extends EventHandler[Json] {
  private[this] val conn: SocketConnection[Json, Json] = new SocketConnection("sandbox", "/connect", binary, this)

  def send(msg: Json) = conn.sendMessage(msg)

  override def onConnect() = dom.document.getElementById("conn-status").innerHTML = "Connected"
  override def onMessage(msg: Json) = dom.document.getElementById("conn-status").innerHTML += s"<div>Received: ${msg.spaces2}</div>"
  override def onError(err: Event) = Logging.info(s"Error: $err")
  override def onClose() = Logging.debug("Connection closed")
}
