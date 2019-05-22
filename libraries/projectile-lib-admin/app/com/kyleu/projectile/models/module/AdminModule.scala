package com.kyleu.projectile.models.module

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Injector, Provides}
import com.kyleu.projectile.graphql.GraphQLSchema
import com.kyleu.projectile.models.config._
import com.kyleu.projectile.models.graphql.EmptySchema
import com.kyleu.projectile.models.menu.MenuProvider
import com.kyleu.projectile.models.notification.Notification
import com.kyleu.projectile.models.status.{AppStatus, StatusProvider}
import com.kyleu.projectile.models.user.{Role, SystemUser}
import com.kyleu.projectile.models.web.{ErrorHandler, GravatarUrl}
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.services.search.SearchProvider
import com.kyleu.projectile.util.JsonSerializers.extract
import com.kyleu.projectile.util.metrics.MetricsConfig
import com.kyleu.projectile.util.tracing.{OpenTracingService, TracingService}
import io.circe.JsonObject
import net.codingwell.scalaguice.ScalaModule

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
  protected[this] def appStatus(app: Application, injector: Injector) = AppStatus(name = projectName)
  protected[this] def searchProvider = new SearchProvider()

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
  protected[this] def errorActions = new ErrorHandler.Actions()
  protected[this] def uiConfigProvider: Application.UiConfigProvider = new Application.UiConfigProvider {
    override def allowRegistration = allowSignup
    override def defaultRole = initialRole
    override def configForUser(su: Option[SystemUser], admin: Boolean, notifications: Seq[Notification], breadcrumbs: String*) = su match {
      case None => UiConfig(projectName = projectName, menu = menuProvider.guestMenu, urls = navUrls)
      case Some(u) =>
        val menu = menuProvider.menuFor(Some(u))
        val theme = extract[JsonObject](u.settings).apply("theme").map(extract[String]).getOrElse("dark")
        val user = u.settingsObj.copy(avatarUrl = Some(GravatarUrl(u.email)))
        val html = NavHtml(com.kyleu.projectile.views.html.components.headerRightMenu(u.username, user.avatarUrl.getOrElse(""), notifications))
        UiConfig(
          projectName = projectName,
          userId = Some(u.id),
          menu = menu,
          urls = navUrls,
          html = html,
          user = user,
          notifications = notifications,
          breadcrumbs = MenuProvider.breadcrumbs(menu, breadcrumbs)
        )
    }
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
  def providesUiConfigProvider: Application.UiConfigProvider = uiConfigProvider
  @Provides @javax.inject.Singleton
  def providesErrorActions() = errorActions
  @Provides @javax.inject.Singleton
  def providesGraphQLSchema(): GraphQLSchema = schema
  @Provides @javax.inject.Singleton
  def providesSearchProvider(): SearchProvider = searchProvider
  @Provides @javax.inject.Singleton
  def providesStatusProvider(): StatusProvider = new StatusProvider {
    override def onAppStartup(app: Application, injector: Injector) = onStartup(app, injector)
    override def getStatus(app: Application, injector: Injector) = appStatus(app, injector)
  }
}
