package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.util.JsonSerializers._

object SourceFileHeader {
  implicit val jsonEncoder: Encoder[SourceFileHeader] = deriveEncoder
  implicit val jsonDecoder: Decoder[SourceFileHeader] = deriveDecoder
}

case class SourceFileHeader(
    projectName: Option[String] = None,
    projectUrl: Option[String] = None,
    authors: Seq[String] = Nil,
    definitionsUrl: Option[String] = None,
    refs: Seq[String] = Nil,
    content: Seq[String] = Nil
) {
  def merge(o: SourceFileHeader) = {
    copy(
      projectName = projectName.orElse(o.projectName),
      projectUrl = projectUrl.orElse(o.projectUrl),
      authors = authors ++ o.authors,
      definitionsUrl = definitionsUrl.orElse(o.definitionsUrl),
      refs = refs ++ o.refs,
      content = content ++ o.content)
  }
}
