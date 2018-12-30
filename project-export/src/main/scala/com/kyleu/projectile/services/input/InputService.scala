package com.kyleu.projectile.services.input

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.database.input.{PostgresConnection, PostgresInput}
import com.kyleu.projectile.models.graphql.input.GraphQLInput
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.thrift.input.{ThriftInput, ThriftOptions}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.database.schema.SchemaHelper
import com.kyleu.projectile.services.thrift.ThriftParseService
import com.kyleu.projectile.util.JsonFileLoader
import com.kyleu.projectile.util.JsonSerializers._

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory

  def list() = dir.children.filter(_.isDirectory).toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / s"input.json"
    if (f.exists && f.isRegularFile && f.isReadable) {
      JsonFileLoader.loadFile[InputSummary](f, "input summary").copy(key = key)
    } else {
      throw new IllegalStateException(s"Cannot load input with key [$key]")
    }
  }

  def load(key: String): Input = {
    val summ = getSummary(key)
    summ.template match {
      case InputTemplate.Postgres => PostgresInputService.loadPostgres(cfg, summ)
      case InputTemplate.Filesystem => throw new IllegalStateException("Unable to load filesystem inputs (coming soon)")
      case InputTemplate.Thrift => ThriftInputService.loadThrift(cfg, summ)
      case InputTemplate.GraphQL => GraphQLInputService.loadGraphQL(cfg, summ)
    }
  }

  def add(is: InputSummary) = {
    val dir = SummaryInputService.saveSummary(cfg, is)
    is.template match {
      case InputTemplate.Postgres => PostgresInputService.savePostgresDefault(cfg, dir)
      case InputTemplate.Filesystem => throw new IllegalStateException("Unable to add filesystem inputs (coming soon)")
      case InputTemplate.Thrift => ThriftInputService.saveThriftDefault(cfg, dir)
      case InputTemplate.GraphQL => GraphQLInputService.saveGraphQLDefault(cfg, dir)
    }
    load(is.key)
  }

  def setPostgresOptions(key: String, conn: PostgresConnection) = {
    val input = PostgresInputService.toPostgresInput(summ = getSummary(key), pc = conn)
    PostgresInputService.savePostgres(cfg, input)
  }

  def setThriftOptions(key: String, to: ThriftOptions) = {
    val input = ThriftInput.fromSummary(is = getSummary(key), files = to.files)
    ThriftInputService.saveThrift(cfg, input)
  }

  def remove(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  def refresh(key: String) = load(key) match {
    case pg: PostgresInput =>
      val conn = pg.newConnection()
      val s = SchemaHelper.calculateSchema(conn)
      conn.close()
      val pgi = pg.copy(enums = s.enums, tables = s.tables, views = s.views)
      PostgresInputService.savePostgres(cfg, pgi)
      pgi
    case t: ThriftInput => ThriftInputService.saveThrift(cfg, t)
    case g: GraphQLInput => GraphQLInputService.saveGraphQL(cfg, g)
    case x => throw new IllegalStateException(s"Unable to process [$x]")
  }
}
