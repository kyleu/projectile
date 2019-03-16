import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("Entrypoint")
class Entrypoint(participantId: String, binary: Boolean) {
  new ConnectionService(participantId, binary).open()
}
