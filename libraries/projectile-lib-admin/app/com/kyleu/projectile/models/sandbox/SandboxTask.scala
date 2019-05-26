package com.kyleu.projectile.models.sandbox

import com.google.inject.Injector
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class SandboxTask(val id: String, val name: String, val description: String) extends Logging {
  SandboxTask.register(this)

  def call(cfg: SandboxTask.Config)(implicit trace: TraceData): Future[String]

  def run(cfg: SandboxTask.Config)(implicit trace: TraceData): Future[SandboxTask.Result] = {
    cfg.tracingService.trace(id + ".sandbox") { sandboxTrace =>
      log.info(s"Running sandbox task [$id]...")
      val startMs = System.currentTimeMillis
      val result = call(cfg)(sandboxTrace).map { r =>
        val res = SandboxTask.Result(this, cfg.argument, "OK", r, (System.currentTimeMillis - startMs).toInt)
        log.info(s"Completed sandbox task [$id] with status [${res.status}] in [${res.elapsed}ms].")
        res
      }
      result
    }
  }
  override def toString = id
}

object SandboxTask {
  private[this] var tasks = Set.empty[SandboxTask]
  def getOpt(id: String) = tasks.find(_.id == id)
  def get(id: String) = getOpt(id).getOrElse(throw new IllegalStateException(s"No registered task with id [$id]"))
  def getAll = tasks.toSeq.sortBy(_.id)

  def register(task: SandboxTask) = getOpt(task.id) match {
    case Some(t) => tasks = tasks - t + task
    case None => tasks = tasks + task
  }

  implicit val jsonEncoder: Encoder[SandboxTask] = (r: SandboxTask) => io.circe.Json.fromString(r.id)
  implicit val jsonDecoder: Decoder[SandboxTask] = (c: io.circe.HCursor) => Right(get(c.as[String].right.get))

  final case class Config(tracingService: TracingService, injector: Injector, argument: Option[String]) {
    def get[T](implicit ct: ClassTag[T]): T = injector.getInstance(ct.runtimeClass).asInstanceOf[T]
  }
  final case class Result(task: SandboxTask, arg: Option[String], status: String = "OK", result: String, elapsed: Int)
  object Result {
    implicit val jsonEncoder: Encoder[Result] = deriveEncoder
    implicit val jsonDecoder: Decoder[Result] = deriveDecoder
  }
}
