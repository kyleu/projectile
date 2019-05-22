package com.kyleu.projectile.services.input

import better.files.File
import com.kyleu.projectile.models.graphql.input.{GraphQLInput, GraphQLOptions}
import com.kyleu.projectile.models.input.{InputSummary, InputTemplate}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.{JacksonUtils, JsonFileLoader}
import com.kyleu.projectile.util.JsonSerializers._
import io.scalaland.chimney.dsl._

object GraphQLInputService {
  private[this] val fn = "graphql-files.json"

  def saveGraphQLDefault(cfg: ConfigService, dir: File) = if (!(dir / fn).exists) {
    (dir / fn).overwrite(JacksonUtils.printJackson(GraphQLOptions.empty.asJson))
  }

  def saveGraphQL(cfg: ConfigService, gi: GraphQLInput) = {
    val summ = gi.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.GraphQL).transform
    val dir = SummaryInputService.saveSummary(cfg, summ)

    val options = JacksonUtils.printJackson(gi.into[GraphQLOptions].transform.asJson)
    (dir / fn).overwrite(options)

    gi
  }

  def loadOptions(cfg: ConfigService, key: String) = {
    JsonFileLoader.loadFile[GraphQLOptions](cfg.inputDirectory / key / fn, "GraphQL query files")
  }

  def loadGraphQL(cfg: ConfigService, summ: InputSummary) = {
    val opts = loadOptions(cfg, summ.key)
    GraphQLInput.fromSummary(summ, opts.schema, cfg.workingDirectory)
  }
}
