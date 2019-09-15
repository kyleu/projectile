package com.kyleu.projectile.controllers.admin.migrate

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.migrate.routes.MigrationController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.queries.migrate.DatabaseMigrationQueries
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.migrate.MigrateTask

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class MigrationController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("migrate") {
  ApplicationFeature.enable(ApplicationFeature.Migrate)
  PermissionService.registerModel("tools", "Migrate", "Database Migrations", Some(InternalIcons.migration), "view", "edit")
  val feature = ApplicationFeature.Migrate.value
  val msg = "Flyway database migrations, to evolve your database"
  SystemMenu.addToolMenu(feature, "Database Migrations", Some(msg), MigrationController.list(), InternalIcons.migration, ("tools", "Migrate", "view"))

  if (app.db.getConfig.runMigrations) {
    app.tracing.topLevelTraceBlocking("migrations") { td =>
      try {
        MigrateTask.migrate(app.db.source)(td)
      } catch {
        case NonFatal(x) => log.error("Error running database migrations", x)(td)
      }
    }
  }

  def list = withSession("list", ("tools", "Migrate", "view")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "migrate")
    val migrations = app.db.query(DatabaseMigrationQueries.getAll(orderBys = Seq(OrderBy("installedRank"))))
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.migrate.migrationList(cfg, migrations)))
  }

  def view(rank: Long) = withSession("view", ("tools", "Migrate", "view")) { implicit request => implicit td =>
    val m = app.db.query(DatabaseMigrationQueries.getByPrimaryKey(rank)).getOrElse(throw new IllegalStateException(s"No migration with rank [$rank]"))
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "migrate", m.version.map("v" + _).getOrElse(m.installedRank.toString))
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.migrate.migrationView(cfg, m)))
  }

  def remove(rank: Long) = withSession("remove", ("tools", "Migrate", "edit")) { _ => implicit td =>
    app.db.execute(DatabaseMigrationQueries.removeByPrimaryKey(rank))
    Future.successful(Redirect(com.kyleu.projectile.controllers.admin.migrate.routes.MigrationController.list()))
  }
}
