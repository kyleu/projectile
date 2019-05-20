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
import com.kyleu.projectile.util.Logging

object ApplicationFeatures extends Logging {
  private[this] var enabledFeatures = Set.empty[String]

  def enabled = enabledFeatures

  def enable(key: String) = {
    key match {
      case "audit" => SystemMenu.addModelMenu(key, "Audits", AuditController.list(), InternalIcons.audit)
      case "encryption" => SystemMenu.addToolMenu(key, "Encryption", EncryptionController.form(), InternalIcons.encryption)
      case "note" => SystemMenu.addModelMenu(key, "Notes", NoteController.list(), InternalIcons.note)
      case "process" => SystemMenu.addToolMenu(key, "Processes", ProcessController.list(), InternalIcons.process)
      case "rest" => SystemMenu.addRootMenu(key, "Swagger UI", OpenApiController.ui(), InternalIcons.rest)
      case "sandbox" => SystemMenu.addToolMenu(key, "Sandbox Tasks", SandboxController.list(), InternalIcons.sandbox)
      case "status" => SystemMenu.addToolMenu(key, "App Status", StatusController.status(), InternalIcons.status)
      case "task" => SystemMenu.addToolMenu(key, "Scheduled Tasks", ScheduleController.list(), InternalIcons.scheduledTaskRun)
      case "user" => SystemMenu.addModelMenu(key, "System Users", SystemUserController.list(), InternalIcons.systemUser)
      case "profile" => SystemMenu.addRootMenu(key, "Profile", ProfileController.view(), InternalIcons.systemUser)
      case "graphql" => SystemMenu.addRootMenu(key, "GraphQL", GraphQLController.iframe(), InternalIcons.graphql)
      case "search" => // noop
      case _ => throw new IllegalStateException(s"Unknown feature [$key]")
    }
    enabledFeatures = enabledFeatures + key
  }
}
