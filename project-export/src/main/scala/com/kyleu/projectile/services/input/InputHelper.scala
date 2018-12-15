package com.kyleu.projectile.services.input

import com.kyleu.projectile.models.command.ProjectileCommand._
import com.kyleu.projectile.models.command.ProjectileResponse.{CompositeResult, InputDetail, InputList}
import com.kyleu.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.kyleu.projectile.models.database.input.PostgresConnection
import com.kyleu.projectile.models.input.InputSummary
import com.kyleu.projectile.models.thrift.input.ThriftOptions
import com.kyleu.projectile.services.ProjectileService

trait InputHelper { this: ProjectileService =>
  private[this] val inputSvc = new InputService(cfg)

  def listInputs() = inputSvc.list()
  def getInput(key: String) = inputSvc.load(key)
  def addInput(summary: InputSummary) = inputSvc.add(summary)
  def setPostgresOptions(key: String, conn: PostgresConnection) = inputSvc.setPostgresOptions(key, conn)
  def setThriftOptions(key: String, to: ThriftOptions) = inputSvc.setThriftOptions(key, to)
  def removeInput(key: String) = inputSvc.remove(key)
  def refreshInput(key: String) = inputSvc.refresh(key)

  protected val processInput: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case Inputs(key) => key match {
      case Some(k) => InputDetail(getInput(k))
      case None => InputList(listInputs())
    }
    case InputAdd(i) => InputDetail(addInput(i))
    case InputPostgresOptions(key, conn) => InputDetail(setPostgresOptions(key, conn))
    case InputThriftOptions(key, opts) => InputDetail(setThriftOptions(key, opts))
    case InputRemove(key) => removeInput(key)
    case InputRefresh(key) => key match {
      case Some(k) => InputDetail(refreshInput(k))
      case None => CompositeResult(listInputs().map(i => InputDetail(refreshInput(i.key))))
    }
  }
}
