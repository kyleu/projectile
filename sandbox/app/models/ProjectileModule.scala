package models

import com.google.inject.Injector
import com.kyleu.projectile.controllers.admin.status.AppStatus
import com.kyleu.projectile.models.config.NotificationService
import com.kyleu.projectile.models.{AdminModule, Application}
import com.kyleu.projectile.models.user.Role
import com.kyleu.projectile.services.audit.{AuditHelper, AuditService}
import com.kyleu.projectile.services.database.MigrateTask
import com.kyleu.projectile.util.tracing.TraceData
import models.template.UserMenu
import util.Version

class ProjectileModule extends AdminModule(projectName = Version.projectName, allowSignup = true, initialRole = Role.Admin, menuProvider = UserMenu) {
  override protected def onStartup(app: Application, injector: Injector) = {
    MigrateTask.migrate(app.db.source)(TraceData.noop)
    NotificationService.setCallback(f = user => Nil)
    AuditHelper.init(appName = projectName, service = injector.getInstance(classOf[AuditService]))
  }

  override protected def appStatus(app: Application, injector: Injector) = AppStatus(
    version = Version.version,
    status = "OK!"
  )
}
