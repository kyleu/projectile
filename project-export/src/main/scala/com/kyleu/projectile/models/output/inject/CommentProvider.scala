package com.kyleu.projectile.models.output.inject

sealed trait CommentProvider {
  def comment(s: String): String
}

object CommentProvider {
  case object Json extends CommentProvider {
    override def comment(s: String) = s"// $s"
  }

  case object Routes extends CommentProvider {
    override def comment(s: String) = s"# $s"
  }

  case object Scala extends CommentProvider {
    override def comment(s: String) = s"/* $s */"
  }

  case object Thrift extends CommentProvider {
    override def comment(s: String) = s"/* $s */"
  }

  case object Twirl extends CommentProvider {
    override def comment(s: String) = s"<!-- $s -->"
  }
}
