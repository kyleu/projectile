package com.kyleu.projectile.util

import scala.concurrent.{ExecutionContext, Future}

object FutureUtils {
  implicit val defaultContext: ExecutionContext = ExecutionContext.global
  implicit val databaseContext: ExecutionContext = ExecutionContext.global
  implicit val serviceContext: ExecutionContext = ExecutionContext.global
  implicit val graphQlContext: ExecutionContext = ExecutionContext.global

  def acc[T, U](seq: Seq[T], f: T => Future[U])(ec: ExecutionContext) = seq.foldLeft(Future.successful(Seq.empty[U])) { (ret, i) =>
    ret.flatMap(s => f(i).map(_ +: s)(ec))(ec)
  }
}
