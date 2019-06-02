package models.module

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.audit.AuditCallbackProvider
import com.kyleu.projectile.models.module.{AdminModule, Application}
import com.kyleu.projectile.models.reporting.ProjectileReport
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.reporting.ReportService
import com.kyleu.projectile.services.status.AppVersions
import com.kyleu.projectile.util.tracing.TraceData
import models.graphql.Schema
import models.search.SearchHelper
import models.template.UserMenu
import services.audit.AuditCallbacks
import util.Version

import scala.concurrent.{ExecutionContext, Future}

class ProjectileModule extends AdminModule(projectName = Version.projectName, allowSignup = true, initialRole = "admin", menuProvider = UserMenu) {
  override protected def onStartup(app: Application, injector: Injector) = {
    AuditCallbackProvider.init(new AuditCallbacks(injector))

    PermissionService.registerRole("manager", "Manager", "A manager, I guess")
    AppVersions.register(v = "0.0.0", title = "First Blood", on = "2019-01-01", desc = Some("Initial version of this application"))

    ReportService.register(new ProjectileReport(key = "test1", title = "Test Report One") {
      override def args = Seq(ProjectileReport.Argument("foo", "string"), ProjectileReport.Argument("bar", "uuid"))

      override def run(user: UUID, args: Map[String, String], injector: Injector)(implicit ec: ExecutionContext, td: TraceData) = {
        val cols = Seq(
          "foo" -> "Foo",
          "bar" -> "Bar",
          "baz" -> "Baz"
        )
        val rows = Seq(
          Seq(Some("a" -> Some(controllers.routes.HomeController.home())), Some("b" -> None), Some("c" -> None)),
          Seq(None, None, None),
          Seq(Some("x" -> None), None, Some("z" -> None))
        )
        Future.successful(cols -> rows)
      }
    })

    /* Start injected startup code */
    /* End injected startup code */
  }

  override protected def searchProvider = new SearchHelper
  override protected def schema = Schema
}
