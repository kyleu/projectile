package com.projectile.services.input

import better.files.File
import com.projectile.models.graphql.input.{GraphQLInput, GraphQLOptions}
import com.projectile.models.input.{InputSummary, InputTemplate}
import com.projectile.services.config.ConfigService
import com.projectile.util.JsonSerializers._
import io.scalaland.chimney.dsl._

object GraphQLInputService {
  private[this] val fn = "graphql-files.json"

  def saveGraphQLDefault(cfg: ConfigService, dir: File) = if (!(dir / fn).exists) {
    (dir / fn).overwrite(GraphQLOptions().asJson.spaces2)
  }

  def saveGraphQL(cfg: ConfigService, gi: GraphQLInput) = {
    val summ = gi.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.Thrift).transform
    val dir = SummaryInputService.saveSummary(cfg, summ)

    val options = gi.into[GraphQLOptions].transform.asJson.spaces2
    (dir / fn).overwrite(options)

    gi
  }

  def loadGraphQL(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key

    val pc = loadFile[GraphQLOptions](dir / fn, "GraphQL query files")
    GraphQLInput.fromSummary(summ, pc.schema)
  }
}
