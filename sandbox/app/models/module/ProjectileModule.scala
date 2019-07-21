package models.module

import com.google.inject.Injector
import com.kyleu.projectile.models.audit.AuditCallbackProvider
import com.kyleu.projectile.models.module.{AdminModule, Application}
import com.kyleu.projectile.models.sandbox.SandboxTask
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.status.AppVersions
import models.graphql.Schema
import models.search.SearchHelper
import models.template.UserMenu
import services.audit.AuditCallbacks
import services.augment.AugmentRegistry
import util.Version

class ProjectileModule extends AdminModule(projectName = Version.projectName, allowSignup = true, initialRole = "admin", menuProvider = UserMenu) {
  override protected def onStartup(app: Application, injector: Injector) = {
    AuditCallbackProvider.init(new AuditCallbacks(injector))

    PermissionService.registerRole("manager", "Manager", "A manager, I guess")
    AppVersions.register(v = "0.0.0", title = "First Blood", on = "2019-01-01", desc = Some("Initial version of this application"))

    SandboxTask.register(TestbedTask)

    inj(injector, classOf[AugmentRegistry])

    /* Start injected startup code */
    /* End injected startup code */
  }

  override protected def searchProvider = new SearchHelper
  override protected def schema = Schema
}
