package com.kyleu.projectile.models.module

import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.controllers.admin.audit.routes.AuditController
import com.kyleu.projectile.controllers.admin.encrypt.routes.EncryptionController
import com.kyleu.projectile.controllers.admin.note.routes.NoteController
import com.kyleu.projectile.controllers.admin.process.routes.ProcessController
import com.kyleu.projectile.controllers.admin.rest.routes.OpenApiController
import com.kyleu.projectile.controllers.admin.sandbox.routes.SandboxController
import com.kyleu.projectile.controllers.admin.status.routes.StatusController
import com.kyleu.projectile.controllers.admin.task.routes.ScheduleController
import com.kyleu.projectile.controllers.admin.user.routes.SystemUserController
import com.kyleu.projectile.controllers.auth.routes.ProfileController
import com.kyleu.projectile.controllers.admin.graphql.routes.GraphQLController
import com.kyleu.projectile.controllers.admin.sql.routes.SqlBackdoorController
// import com.kyleu.projectile.controllers.sitemap.routes.SitemapController
import com.kyleu.projectile.util.Logging

object ApplicationFeatures extends Logging {
  private[this] var enabledFeatures = Set.empty[String]

  def enabled = enabledFeatures

  def enable(key: String) = if (!enabled(key)) {
    key match {
      case "audit" => SystemMenu.addModelMenu(key, "Audits", Some("System audits provide detailed change logging"), AuditController.list(), InternalIcons.audit)
      case "connection" => // noop
      case "encryption" => SystemMenu.addToolMenu(key, "Encryption", Some("Allows you to encrypt and decrypt strings using the system keys"), EncryptionController.form(), InternalIcons.encryption)
      case "graphql" => SystemMenu.addRootMenu(key, "GraphQL", Some("A full GraphQL IDE and schema visualizer"), GraphQLController.iframe(), InternalIcons.graphql)
      case "note" => SystemMenu.addModelMenu(key, "Notes", Some("You can log notes on most pages, this lets you manage them"), NoteController.list(), InternalIcons.note)
      case "process" => SystemMenu.addToolMenu(key, "Processes", Some("Run processes on the application server (dangerous)"), ProcessController.list(), InternalIcons.process)
      case "profile" => SystemMenu.addRootMenu(key, "Profile", Some("View your system profile"), ProfileController.view(), InternalIcons.systemUser)
      case "rest" => SystemMenu.addRootMenu(key, "Swagger UI", Some("OpenAPI IDE for exploring the system"), OpenApiController.ui(), InternalIcons.rest)
      case "sandbox" => SystemMenu.addToolMenu(key, "Sandbox Tasks", Some("Simple one-off tasks that can be run through this UI"), SandboxController.list(), InternalIcons.sandbox)
      case "search" => // noop
      case "sitemap" => // noop SystemMenu.addRootMenu(key, "Sitemap", Some("The sitemap of this application"), SitemapController.sitemap(), InternalIcons.sitemap)
      case "sql" => SystemMenu.addToolMenu(key, "SQL Access", Some("A SQL prompt for the application database (dangerous)"), SqlBackdoorController.sql(), InternalIcons.sql)
      case "status" => SystemMenu.addToolMenu(key, "App Status", Some("View the status of this application"), StatusController.status(), InternalIcons.status)
      case "task" => SystemMenu.addToolMenu(key, "Scheduled Tasks", Some("View the history and configuration of scheduled tasks"), ScheduleController.list(), InternalIcons.scheduledTaskRun)
      case "user" => SystemMenu.addModelMenu(key, "System Users", Some("Manage the system users of this application"), SystemUserController.list(), InternalIcons.systemUser)
      case _ => throw new IllegalStateException(s"Unknown feature [$key]")
    }
    enabledFeatures = enabledFeatures + key
  }
}
