package com.kyleu.projectile.services.input

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.database.input.{PostgresConnection, PostgresInput}
import com.kyleu.projectile.models.graphql.input.GraphQLInput
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.thrift.input.{ThriftInput, ThriftOptions}
import com.kyleu.projectile.models.typescript.input.{TypeScriptInput, TypeScriptOptions}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.database.schema.SchemaHelper
import com.kyleu.projectile.util.JsonFileLoader
import com.kyleu.projectile.util.tracing.TraceData

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory

  def immediateList() = if (dir.exists) {
    dir.children.filter(_.isDirectory).toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary(_))
  } else {
    Nil
  }

  def list(): Seq[InputSummary] = immediateList() ++ cfg.linkedConfigs.map(c => new InputService(c)).flatMap(_.list()).sortBy(_.key)

  def getSummary(key: String, observed: Seq[String] = Nil): InputSummary = {
    val f = dir / key / "input.json"
    if (f.exists && f.isRegularFile && f.isReadable) {
      JsonFileLoader.loadFile[InputSummary](f, "input summary").copy(key = key)
    } else {
      cfg.configForInput(key) match {
        case Some(c) =>
          val path = f.pathAsString
          if (observed.contains(path)) {
            throw new IllegalStateException(s"Loop detected with input [$key]: ${observed.mkString(", ")}")
          }
          new InputService(c).getSummary(key, observed :+ path)
        case _ => throw new IllegalStateException(s"Cannot load input with key [$key]")
      }
    }
  }

  def load(key: String): Input = {
    val summ = getSummary(key)
    val c = configFor(key)
    summ.template match {
      case InputTemplate.Postgres => PostgresInputService.loadPostgres(c, summ)
      case InputTemplate.Thrift => ThriftInputService.loadThrift(c, summ)
      case InputTemplate.GraphQL => GraphQLInputService.loadGraphQL(c, summ)
      case InputTemplate.TypeScript => TypeScriptInputService.loadTypeScript(c, summ)
    }
  }

  def add(is: InputSummary) = {
    val dir = SummaryInputService.saveSummary(cfg, is)
    is.template match {
      case InputTemplate.Postgres => PostgresInputService.savePostgresDefault(cfg, dir)
      case InputTemplate.Thrift => ThriftInputService.saveThriftDefault(cfg, dir)
      case InputTemplate.GraphQL => GraphQLInputService.saveGraphQLDefault(cfg, dir)
      case InputTemplate.TypeScript => TypeScriptInputService.saveTypeScriptDefault(cfg, dir)
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

  def setTypeScriptOptions(key: String, to: TypeScriptOptions) = {
    val input = TypeScriptInput.fromSummary(is = getSummary(key), files = to.files)
    TypeScriptInputService.saveTypeScript(cfg, input)
  }

  def remove(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK(s"Removed input [$key]")
  }

  def refresh(key: String) = load(key) match {
    case pg: PostgresInput =>
      val conn = pg.newConnection()
      val s = SchemaHelper.calculateSchema(conn)(TraceData.noop)
      conn.close()
      val pgi = pg.copy(enumTypes = s.enums, tables = s.tables, views = s.views)
      PostgresInputService.savePostgres(cfg, pgi)
      pgi
    case t: ThriftInput => ThriftInputService.saveThrift(cfg, t)
    case g: GraphQLInput => GraphQLInputService.saveGraphQL(cfg, g)
    case t: TypeScriptInput => TypeScriptInputService.saveTypeScript(cfg, t)
    case x => throw new IllegalStateException(s"Unable to process [$x]")
  }

  private def configFor(key: String) = cfg.configForInput(key).getOrElse(throw new IllegalStateException(s"No config found with key [$key]"))
}
