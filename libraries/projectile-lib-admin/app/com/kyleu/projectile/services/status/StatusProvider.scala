package com.kyleu.projectile.services.status

import com.google.inject.Injector
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.status.AppStatus

trait StatusProvider {
  def onAppStartup(app: Application, injector: Injector): Unit
  def getStatus(app: Application, injector: Injector): AppStatus
}
