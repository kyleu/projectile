package models.module

import com.google.inject.Injector
import com.kyleu.projectile.models.audit.AuditCallbackProvider
import com.kyleu.projectile.models.module.{AdminModule, Application}
import com.kyleu.projectile.models.status.AppStatus
import com.kyleu.projectile.services.audit.{AuditHelper, AuditService}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.notification.NotificationService
import models.graphql.Schema
import models.search.SearchHelper
import models.template.{Icons, UserMenu}
import services.audit.AuditCallbacks
import util.Version

class ProjectileModule extends AdminModule(projectName = Version.projectName, allowSignup = true, initialRole = "admin", menuProvider = UserMenu) {
  override protected def onStartup(app: Application, injector: Injector) = {
    AuditCallbackProvider.init(new AuditCallbacks(injector))

    PermissionService.registerRole("manager", "Manager", "A manager, I guess")

    /* Start injected startup code */
    /* End injected startup code */
  }

  override protected def appStatus(app: Application, injector: Injector) = {
    AppStatus(name = projectName, version = Version.version, status = "OK!")
  }

  override protected def searchProvider = new SearchHelper

  override protected def schema = Schema
}
