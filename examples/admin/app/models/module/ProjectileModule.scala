package models.module

import com.google.inject.Injector
import com.kyleu.projectile.graphql.GraphQLSchema
import com.kyleu.projectile.models.audit.AuditCallbackProvider
import com.kyleu.projectile.models.status.AppStatus
import com.kyleu.projectile.models.module.{AdminModule, Application}
import services.audit.AuditCallbacks
import models.graphql.Schema
import models.search.SearchHelper
import models.template.UserMenu
import util.Version

class ProjectileModule extends AdminModule {
  override def projectVersion = Version.version
  override def projectName = Version.projectName
  override def allowSignup = true
  override def initialRole = "admin"
  override def menuProvider = UserMenu

  override protected def onStartup(app: Application, injector: Injector) = {
    AuditCallbackProvider.init(new AuditCallbacks(injector))

    /* Start injected startup code */
    /* End injected startup code */
  }

  override protected def appStatus(app: Application, injector: Injector) = AppStatus(name = projectName, version = Version.version)
  override protected def searchProvider = new SearchHelper
  override protected def schema = Schema
}
