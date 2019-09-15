package models.module

import com.google.inject.Injector
import com.kyleu.projectile.models.audit.AuditCallbackProvider
import com.kyleu.projectile.models.module.{AdminModule, Application}
import com.kyleu.projectile.models.sandbox.SandboxTask
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.status.AppVersions
import com.kyleu.projectile.services.task.ScheduledTaskRegistry
import models.graphql.Schema
import models.sandbox.TestbedTask
import models.search.SearchHelper
import models.task.TestTask
import models.template.UserMenu
import services.audit.AuditCallbacks
import services.augment.AugmentRegistry
import util.Version

class ProjectileModule extends AdminModule() {
  override def projectName = Version.projectName
  override def allowSignup = true
  override def initialRole = "user"
  override def menuProvider = UserMenu

  override protected def onStartup(app: Application, injector: Injector) = {
    AuditCallbackProvider.init(new AuditCallbacks(injector))

    PermissionService.registerRole("manager", "Manager", "A manager, I guess")
    AppVersions.register(v = "0.0.0", title = "First Blood", on = "2019-01-01", desc = Some("Initial version of this application"))

    SandboxTask.register(TestbedTask)

    inj(injector, classOf[AugmentRegistry])

    ScheduledTaskRegistry.register(TestTask)

    /* Start injected startup code */
    /* Projectile export section [sandbox] */
    com.kyleu.projectile.services.auth.PermissionService.registerPackage("b", "B", models.template.Icons.pkg_b)
    com.kyleu.projectile.services.auth.PermissionService.registerPackage("t", "T", models.template.Icons.pkg_t)
    /* End injected startup code */
  }

  override protected def searchProvider = new SearchHelper
  override protected def schema = Schema
}
