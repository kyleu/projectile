package com.projectile.services.input

import com.projectile.models.command.ProjectileCommand._
import com.projectile.models.command.ProjectileResponse.{InputDetail, InputList}
import com.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.projectile.models.database.input.PostgresConnection
import com.projectile.models.input.InputSummary
import com.projectile.services.ProjectileService

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
