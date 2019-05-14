package com.kyleu.projectile.services.status

import com.google.inject.Injector
import com.kyleu.projectile.controllers.admin.status.AppStatus
import com.kyleu.projectile.models.Application

class StatusProvider {
  def onAppStartup(app: Application, injector: Injector) = {}
  def getStatus(app: Application, injector: Injector) = AppStatus()
}
