package models.template

import com.kyleu.projectile.models.config.{MenuProvider, NavMenu}
import com.kyleu.projectile.models.user.SystemUser

object UserMenu extends MenuProvider {
  private[this] lazy val staticSandbox = NavMenu(key = "sandbox", title = "Sandbox", url = Some(controllers.routes.HomeController.sandbox().url))
  private[this] lazy val staticGraphQL = {
    import com.kyleu.projectile.controllers.graphql
    NavMenu(key = "graphql", title = "GraphQL", url = Some(graphql.routes.GraphQLController.graphql().url), children = Seq(
      NavMenu(key = "ide", title = "IDE", url = Some(graphql.routes.GraphQLController.graphql().url), icon = Some("brightness_low")),
      NavMenu(key = "query", title = "Query Schema", url = Some(graphql.routes.SchemaController.voyager("Query").url), icon = Some("brightness_high")),
      NavMenu(key = "mutation", title = "Mutation Schema", url = Some(graphql.routes.SchemaController.voyager("Query").url), icon = Some("brightness_high"))
    ), flatSection = true)
  }

  private[this] lazy val staticRest = {
    NavMenu(key = "rest", title = "Rest", url = Some(com.kyleu.projectile.controllers.rest.routes.OpenApiController.ui().url), children = Seq(
      NavMenu(key = "ide", title = "Swagger UI", url = Some(com.kyleu.projectile.controllers.rest.routes.OpenApiController.ui().url), icon = Some("dialpad"))
    ), flatSection = true)
  }

  private[this] lazy val staticStatus = NavMenu(key = "status", title = "Status", children = Seq(
    NavMenu(key = "notes", title = "Notes", url = Some(com.kyleu.projectile.controllers.admin.note.routes.NoteController.list().url), icon = Some("folder_open")),
    NavMenu(key = "audit", title = "Audits", url = Some(com.kyleu.projectile.controllers.admin.audit.routes.AuditController.list().url), icon = Some("memory")),
    NavMenu(key = "user", title = "System Users", url = Some(com.kyleu.projectile.controllers.admin.user.routes.SystemUserController.list().url), icon = Some("account_circle"))
  ), flatSection = true)

  private[this] lazy val staticMenu = Seq(staticSandbox) ++ ComponentMenu.menu ++ Seq(staticGraphQL, staticRest, staticStatus)

  override def adminMenu(u: SystemUser) = staticMenu
}
