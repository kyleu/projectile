package services.helper

import models.command.{ProjectileCommand, ProjectileResponse}
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.database.input.PostgresConnection
import models.input.InputSummary
import services.ProjectileService
import services.input.InputService

trait InputHelper { this: ProjectileService =>
  private[this] val inputSvc = new InputService(cfg)

  def listInputs() = inputSvc.list()
  def getInput(key: String) = inputSvc.load(key)
  def addInput(summary: InputSummary) = inputSvc.add(summary)
  def setPostgresOptions(key: String, conn: PostgresConnection) = inputSvc.setPostgresOptions(key, conn)
  def removeInput(key: String) = inputSvc.remove(key)
  def refreshInput(key: String) = inputSvc.refresh(key)

  protected val processInput: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListInputs => InputList(listInputs())
    case GetInput(key) => InputDetail(getInput(key))
    case AddInput(i) => InputDetail(addInput(i))
    case SetPostgresOptions(key, conn) => InputDetail(setPostgresOptions(key, conn))
    case RemoveInput(key) => removeInput(key)
    case RefreshInput(key) => InputDetail(refreshInput(key))
  }
}
