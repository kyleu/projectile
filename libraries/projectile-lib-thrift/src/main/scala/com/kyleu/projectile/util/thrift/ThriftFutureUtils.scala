package com.kyleu.projectile.util.thrift

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object ThriftFutureUtils {
  implicit val defaultContext: ExecutionContext = ExecutionContext.global

  def acc[T, U](seq: Seq[T], f: T => Future[U])(ec: ExecutionContext) = seq.foldLeft(Future.successful(Seq.empty[U])) { (ret, i) =>
    ret.flatMap(s => f(i).map(_ +: s)(ec))(ec)
  }

  implicit def toScalaTry[T](t: com.twitter.util.Try[T]): Try[T] = t match {
    case com.twitter.util.Return(r) => Success(r)
    case com.twitter.util.Throw(ex) => Failure(ex)
  }

  implicit def toScalaFuture[T](f: com.twitter.util.Future[T]): Future[T] = {
    val promise = Promise[T]()
    f.respond(x => promise.complete(toScalaTry(x)))
    promise.future
  }
}
