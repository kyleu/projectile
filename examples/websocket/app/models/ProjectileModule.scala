package models

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.kyleu.projectile.controllers.websocket.WebsocketController
import com.kyleu.projectile.graphql.GraphQLSchema
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.AuthActions
import com.kyleu.projectile.models.user.Role
import com.kyleu.projectile.services.connection.ConnectionSupervisor
import com.kyleu.projectile.services.note.NoteService
import com.kyleu.projectile.util.metrics.MetricsConfig
import com.kyleu.projectile.util.tracing.TracingService
import com.kyleu.projectile.web.util.ErrorHandler
import net.codingwell.scalaguice.ScalaModule
import services.note.ModelNoteService

class ProjectileModule extends AbstractModule with ScalaModule {
  override def configure() = {
    bind[NoteService].to(classOf[ModelNoteService])
  }

  @Provides @javax.inject.Singleton
  def provideTracingService(cnf: MetricsConfig): TracingService = TracingService.noop

  @Provides @javax.inject.Singleton
  def provideApplicationActions = Application.Actions(projectName = util.Config.projectName)

  @Provides @javax.inject.Singleton
  def providesErrorActions() = new ErrorHandler.Actions()

  @Provides @javax.inject.Singleton
  def providesAuthActions() = new AuthActions(projectName = util.Config.projectName) {
    override def defaultRole: Role = Role.Admin
    override def adminMenu = views.html.admin.layout.menu.apply
  }

  @Provides @javax.inject.Singleton
  def provideGraphQLSchema(): GraphQLSchema = models.graphql.Schema

  @Provides @javax.inject.Singleton @Named("connection-supervisor")
  def provideConnectionSupervisor(actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(ConnectionSupervisor.props(err = WebsocketController.errJson), "connections")
  }
}
