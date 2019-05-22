package com.kyleu.projectile.services.augment

import com.kyleu.projectile.models.config.UiConfig
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import play.twirl.api.Html

import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal
import scala.concurrent.duration._
import scala.reflect.ClassTag

object AugmentService {
  val views = new AugmentService[Html]()
}

class AugmentService[Rsp] extends Logging {
  private[this] var map = Map.empty[Class[_], (Any, Map[String, Seq[String]], UiConfig) => Future[Option[Rsp]]]

  def register[T](f: (T, Map[String, Seq[String]], UiConfig) => Future[Option[Rsp]])(implicit tag: ClassTag[T]) = {
    map = map + (tag.runtimeClass -> f.asInstanceOf[(Any, Map[String, Seq[String]], UiConfig) => Future[Option[Rsp]]])
  }

  def augment[T](model: T, args: Map[String, Seq[String]], cfg: UiConfig): Option[Rsp] = {
    map.get(model.getClass).flatMap(f => extract(f.asInstanceOf[(T, Map[String, Seq[String]], UiConfig) => Future[Option[Rsp]]](model, args, cfg)))
  }

  private[this] def extract(f: Future[Option[Rsp]]) = try { Await.result(f, 15.seconds) } catch {
    case NonFatal(x) =>
      log.error(s"Augmentation error", x)(TraceData.noop)
      None
  }
}
