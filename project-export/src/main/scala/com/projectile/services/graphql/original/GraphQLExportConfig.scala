package com.projectile.services.graphql.original

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

object GraphQLExportConfig {
  implicit val jsonEncoder: Encoder[GraphQLExportConfig] = deriveEncoder
  implicit val jsonDecoder: Decoder[GraphQLExportConfig] = deriveDecoder
}

case class GraphQLExportConfig(
    input: String, output: String, providedPrefix: Seq[String], pkg: String, modelPkg: Seq[String], schema: Option[String], pkgs: Map[String, Seq[String]]
) {
  def pkgSeq = pkg.split('.').map(_.trim).filter(_.nonEmpty)
  def pkgFor(n: String) = pkgs.getOrElse(n, modelPkg)
}
