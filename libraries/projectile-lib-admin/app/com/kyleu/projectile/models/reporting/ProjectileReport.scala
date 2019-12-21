package com.kyleu.projectile.models.reporting

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.auth.Permission
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc.Call

import scala.concurrent.{ExecutionContext, Future}

object ProjectileReport {
  object Argument {
    implicit val jsonEncoder: Encoder[Argument] = (r: Argument) => io.circe.Json.obj(
      ("key", r.key.asJson),
      ("t", r.t.asJson),
      ("optional", r.optional.asJson)
    )
  }

  final case class Argument(key: String, t: String, optional: Boolean = false)

  implicit val jsonEncoder: Encoder[ProjectileReport] = (r: ProjectileReport) => io.circe.Json.obj(
    ("key", r.key.asJson),
    ("title", r.title.asJson),
    ("args", r.args.asJson),
    ("permissions", r.permissions.asJson)
  )
}

abstract class ProjectileReport(
    val key: String,
    val title: String,
    val permissions: Seq[Permission.Path] = Nil
) {
  def args: Seq[ProjectileReport.Argument] = Nil
  def arg(key: String) = args.find(_.key == key).getOrElse(throw new IllegalStateException(s"No argument with key [$key]"))
  def run(
    user: UUID, args: Map[String, String], injector: Injector
  )(implicit ec: ExecutionContext, td: TraceData): Future[(Seq[(String, String)], Seq[Seq[Option[(Any, Option[Call])]]])]

  protected def stringArg(key: String, m: Map[String, String]) = m.get(key).filterNot(_.isEmpty) match {
    case Some(x) => Some(x)
    case None if arg(key).optional => None
    case None => throw new IllegalStateException(s"Argument [$key] is required, and was not provided")
  }
  protected def uuidArg(key: String, m: Map[String, String]) = stringArg(key, m).map(UUID.fromString)
  protected def ldtArg(key: String, m: Map[String, String]) = stringArg(key, m).map(DateUtils.fromString)
}
