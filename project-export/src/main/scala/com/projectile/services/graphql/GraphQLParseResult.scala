package com.projectile.services.graphql

import sangria.ast.Document

case class GraphQLParseResult(
    filename: String,
    pkg: Seq[String],
    document: Document,
    lines: Seq[String]
)
