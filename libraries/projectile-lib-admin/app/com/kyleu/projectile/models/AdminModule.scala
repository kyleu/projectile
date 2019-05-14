package com.kyleu.projectile.models

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Injector, Provides}
import com.kyleu.projectile.controllers.admin.status.AppStatus
import com.kyleu.projectile.graphql.{EmptySchema, GraphQLSchema}
import com.kyleu.projectile.models.auth.AuthActions
import com.kyleu.projectile.models.config._
import com.kyleu.projectile.models.user.Role
import com.kyleu.projectile.models.web.{ErrorHandler, GravatarUrl}
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.services.status.StatusProvider
import com.kyleu.projectile.util.JsonSerializers.extract
import com.kyleu.projectile.util.metrics.MetricsConfig
import com.kyleu.projectile.util.tracing.{OpenTracingService, TracingService}
import io.circe.JsonObject
import net.codingwell.scalaguice.ScalaModule
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext

abstract class AdminModule(
    val projectName: String,
    val allowSignup: Boolean,
    val initialRole: Role,
    val oauthProviders: Seq[String] = Nil,
    val menuProvider: MenuProvider
) extends AbstractModule with ScalaModule {
  // System
  protected[this] def onStartup(app: Application, injector: Injector) = {}
  protected[this] def appStatus(app: Application, injector: Injector) = AppStatus()

  // Tracing
  protected[this] def tracingService(cnf: MetricsConfig, ec: ExecutionContext): TracingService = new OpenTracingService(cnf)(ec)

  // Database
  private[this] var db: Option[JdbcDatabase] = None
  protected[this] def database(ec: ExecutionContext): JdbcDatabase = db.getOrElse {
    val d = new JdbcDatabase("application", "database.application")(ec)
    db = Some(d)
    d
  }
  protected[this] def systemDatabase(ec: ExecutionContext): JdbcDatabase = db.getOrElse {
    val d = new JdbcDatabase("application", "database.application")(ec)
    db = Some(d)
    d
  }

  // UI
  protected[this] def navUrls: NavUrls = NavUrls(signupAllowed = allowSignup, oauthProviders = oauthProviders)
  protected[this] def applicationActions = Application.Actions(
    projectName = projectName,
    configForUser = (su, _, notifications, crumbs) => su match {
      case None => UiConfig(projectName = projectName, menu = menuProvider.guestMenu, urls = navUrls)
      case Some(u) =>
        val menu = menuProvider.menuFor(Some(u))
        val theme = extract[JsonObject](u.settings).apply("theme").map(extract[String]).getOrElse("dark")
        val user = UserSettings(name = u.username, theme = theme, avatarUrl = Some(GravatarUrl(u.email)))
        val html = NavHtml(com.kyleu.projectile.views.html.components.headerRightMenu(user.name, user.avatarUrl.getOrElse(""), notifications))
        UiConfig(
          projectName = projectName,
          menu = menu,
          urls = navUrls,
          html = html,
          user = user,
          notifications = notifications,
          breadcrumbs = menuProvider.breadcrumbs(menu, crumbs)
        )
    }
  )

  protected[this] def errorActions = new ErrorHandler.Actions()
  protected[this] def authActions: AuthActions = new AuthActions(projectName = projectName) {
    override def allowRegistration = allowSignup
    override def defaultRole = initialRole
  }

  // GraphQL
  protected[this] def schema: GraphQLSchema = EmptySchema

  // Guice
  @Provides @javax.inject.Singleton
  def providesTracingService(cnf: MetricsConfig, ec: ExecutionContext): TracingService = tracingService(cnf, ec)
  @Provides @javax.inject.Singleton
  def providesJdbcDatabase(ec: ExecutionContext): JdbcDatabase = database(ec)
  @Provides @javax.inject.Singleton @Named("system")
  def providesSystemJdbcDatabase(ec: ExecutionContext): JdbcDatabase = systemDatabase(ec)
  @Provides @javax.inject.Singleton
  def providesApplicationActions: Application.Actions = applicationActions
  @Provides @javax.inject.Singleton
  def providesErrorActions() = errorActions
  @Provides @javax.inject.Singleton
  def providesAuthActions() = authActions
  @Provides @javax.inject.Singleton
  def providesGraphQLSchema(): GraphQLSchema = schema
  @Provides @javax.inject.Singleton
  def providesStatusProvider(): StatusProvider = new StatusProvider {
    override def onAppStartup(app: Application, injector: Injector) = onStartup(app, injector)
    override def getStatus(app: Application, injector: Injector) = appStatus(app, injector)
  }
}
