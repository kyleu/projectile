package com.kyleu.projectile.controllers.admin.graphql

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.graphql.GraphQLSchema
import com.kyleu.projectile.models.module.Application
import sangria.renderer.SchemaRenderer

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SchemaController @javax.inject.Inject() (
    override val app: Application, schema: GraphQLSchema
)(implicit ec: ExecutionContext) extends AuthController("schema") {
  lazy val idl = SchemaRenderer.renderSchema(schema.schema)

  def renderSchema() = withSession("graphql.schema", admin = true) { implicit request => implicit td =>
    Future.successful(Ok(idl))
  }

  def voyager(root: String) = withSession("schema.render", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "admin", "system", "graphql")
    Future.successful(Ok(com.kyleu.projectile.views.html.graphql.voyager(root = root, cfg = cfg)))
  }
}
