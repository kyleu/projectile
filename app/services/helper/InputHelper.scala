package services.helper

import models.command.{ProjectileCommand, ProjectileResponse}
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.input.InputSummary
import services.ProjectileService
import services.input.InputService

trait InputHelper { this: ProjectileService =>
  private[this] val inputSvc = new InputService(cfg)

  protected val processInput: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListInputs => InputList(inputSvc.list())
    case GetInput(key) => InputDetail(inputSvc.load(key))
    case AddInput(i) => InputDetail(inputSvc.add(i))
    case RemoveInput(key) => inputSvc.remove(key)
    case RefreshInput(key) => InputDetail(inputSvc.refresh(key))
  }

  def listInputs() = process(ListInputs).asInstanceOf[InputList].inputs
  def getInput(key: String) = process(GetInput(key)).asInstanceOf[InputDetail].input
  def addInput(summary: InputSummary) = process(AddInput(summary)).asInstanceOf[InputDetail].input
  def removeInput(key: String) = process(RemoveInput(key))
  def refreshInput(key: String) = process(RefreshInput(key)).asInstanceOf[InputDetail].input
}
