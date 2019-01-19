package com.kyleu.projectile.services.input

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.database.input.{PostgresConnection, PostgresInput}
import com.kyleu.projectile.models.graphql.input.GraphQLInput
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.thrift.input.{ThriftInput, ThriftOptions}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.database.schema.SchemaHelper
import com.kyleu.projectile.util.JsonFileLoader

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory

  def immediateList() = if (dir.exists) {
    dir.children.filter(_.isDirectory).toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)
  } else {
    Nil
  }

  def list(): Seq[InputSummary] = immediateList() ++ cfg.linkedConfigs.map(c => new InputService(c)).flatMap(_.list()).sortBy(_.key)

  def getSummary(key: String): InputSummary = {
    val f = dir / key / s"input.json"
    if (f.exists && f.isRegularFile && f.isReadable) {
      JsonFileLoader.loadFile[InputSummary](f, "input summary").copy(key = key)
    } else {
      cfg.configForInput(key) match {
        case Some(c) => new InputService(c).getSummary(key)
        case _ => throw new IllegalStateException(s"Cannot load input with key [$key]")
      }
    }
  }

  def load(key: String): Input = {
    val summ = getSummary(key)
    val c = configFor(key)
    summ.template match {
      case InputTemplate.Postgres => PostgresInputService.loadPostgres(c, summ)
      case InputTemplate.Filesystem => throw new IllegalStateException("Unable to load filesystem inputs (coming soon)")
      case InputTemplate.Thrift => ThriftInputService.loadThrift(c, summ)
      case InputTemplate.GraphQL => GraphQLInputService.loadGraphQL(c, summ)
    }
  }

  def add(is: InputSummary) = {
    val c = configFor(is.key)
    val dir = SummaryInputService.saveSummary(c, is)
    is.template match {
      case InputTemplate.Postgres => PostgresInputService.savePostgresDefault(c, dir)
      case InputTemplate.Filesystem => throw new IllegalStateException("Unable to add filesystem inputs (coming soon)")
      case InputTemplate.Thrift => ThriftInputService.saveThriftDefault(c, dir)
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

  private def configFor(key: String) = cfg.configForInput(key).getOrElse(throw new IllegalStateException(s"No config found with key [$key]"))
}
