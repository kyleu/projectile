import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import com.kyleu.projectile.util.JsonSerializers._

@JSExportTopLevel("Entrypoint")
class Entrypoint(binary: Boolean) {
  val cs = new ConnectionService(binary)

  @JSExport
  def send(msg: String) = cs.send(msg.asJson)
}
