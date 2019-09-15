package com.kyleu.projectile.services.database.doobie

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}

import cats.Reducible
import cats.effect.{Blocker, ContextShift, IO}
import com.kyleu.projectile.models.tag.Tag
import com.kyleu.projectile.util.JsonSerializers
import doobie.free._
import doobie.implicits._
import doobie.postgres.{Instances => PInstances}
import doobie.syntax.AllSyntax
import doobie.util.{MetaConstructors, MetaInstances}
import doobie.util.transactor.Transactor
import io.circe.Json
import javax.sql.DataSource

import scala.concurrent.ExecutionContext

object DoobieQueryService {
  object Imports extends PInstances with Instances with AllSyntax with doobie.Aliases with doobie.hi.Modules
    with Modules with Types with MetaConstructors with MetaInstances {

    import scala.language.higherKinds

    implicit val localDateTimeMeta: Meta[LocalDateTime] = {
      Meta[Timestamp].timap(ts => ts.toLocalDateTime)(dt => Timestamp.from(dt.toInstant(ZoneOffset.UTC)))
    }
    implicit val zonedDateTimeMeta: Meta[ZonedDateTime] = {
      Meta[Timestamp].timap(ts => ZonedDateTime.from(ts.toLocalDateTime))(dt => Timestamp.from(dt.toInstant))
    }
    implicit val jsonMeta: Meta[Json] = Meta[String].timap(s => JsonSerializers.readJson(s))(j => j.spaces2)
    implicit val tagsMeta: Meta[List[Tag]] = Meta[Map[String, String]].timap(Tag.fromMap)(Tag.toMap)

    def in[F[_]: Reducible, A: Put](f: Fragment, fs: F[A]): Fragment = doobie.Fragments.in(f, fs)
    def notIn[F[_]: Reducible, A: Put](f: Fragment, fs: F[A]): Fragment = doobie.Fragments.notIn(f, fs)
    def whereAndOpt(fs: Option[Fragment]*) = doobie.Fragments.whereAndOpt(fs: _*)
    def whereAnd(fs: Fragment*) = doobie.Fragments.whereAnd(fs: _*)
  }
}

class DoobieQueryService(dataSource: DataSource)(implicit ec: ExecutionContext) {
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val db = Transactor.fromDataSource[IO](dataSource, ec, Blocker.liftExecutionContext(ec))

  def runSync[T](x: doobie.ConnectionIO[T]) = x.transact(db).unsafeRunSync
}
