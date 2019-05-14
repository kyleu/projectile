package com.kyleu.projectile.models.config

import com.kyleu.projectile.controllers
import com.kyleu.projectile.controllers.rest.routes.OpenApiController
import com.kyleu.projectile.controllers.graphql.routes.GraphQLController
import com.kyleu.projectile.controllers.admin.sandbox.routes.SandboxController
import com.kyleu.projectile.controllers.admin.encrypt.routes.EncryptionController
import com.kyleu.projectile.controllers.admin.status.routes.StatusController
import com.kyleu.projectile.models.web.InternalIcons

object NavMenu {
  val encryption = NavMenu(key = "encryption", title = "Encryption", url = Some(EncryptionController.form().url), icon = Some(InternalIcons.encryption))
  val sandbox = NavMenu(key = "sandbox", title = "Sandbox Tasks", url = Some(SandboxController.list().url), icon = Some(InternalIcons.sandbox))

  val graphql = NavMenu(key = "graphql", title = "GraphQL", url = Some(GraphQLController.graphql().url), icon = Some(InternalIcons.graphql))
  val rest = NavMenu(key = "rest", title = "Swagger UI", url = Some(OpenApiController.ui().url), icon = Some(InternalIcons.rest))

  val audits = NavMenu("audit", "Audits", Some(controllers.admin.audit.routes.AuditController.list().url), Some(InternalIcons.audit))
  val notes = NavMenu("notes", "Notes", Some(controllers.admin.note.routes.NoteController.list().url), Some(InternalIcons.note))
  val users = NavMenu("user", "System Users", Some(controllers.admin.user.routes.SystemUserController.list().url), Some(InternalIcons.systemUser))

  val status = NavMenu(key = "status", title = "App Status", url = Some(StatusController.status().url), icon = Some(InternalIcons.status))

  val all = Seq(encryption, sandbox, graphql, rest, audits, notes, users, status)

  val system = NavMenu(key = "system", title = "System", children = all, flatSection = true)
}

case class NavMenu(
    key: String,
    title: String,
    url: Option[String] = None,
    icon: Option[String] = None,
    children: Seq[NavMenu] = Nil,
    flatSection: Boolean = false
)

