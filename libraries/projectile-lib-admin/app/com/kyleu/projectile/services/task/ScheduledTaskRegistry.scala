package com.kyleu.projectile.services.task

import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.task.ScheduledTask
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object ScheduledTaskRegistry {
  private[this] var all = List.empty[ScheduledTask]
  def register(task: ScheduledTask) = all = all :+ task
  def getAll = all

  def byKey(key: String) = all.find(_.key == key).getOrElse(throw new IllegalStateException(s"No task with key [$key]."))

  def runAll(creds: UserCredentials, tasks: Seq[ScheduledTask], log: String => Unit)(implicit td: TraceData) = {
    Future.sequence(tasks.map(t => run(creds, t, log).map(t -> _))).map(_.toMap)
  }

  def run(creds: UserCredentials, task: ScheduledTask, log: String => Unit)(implicit td: TraceData) = {
    val start = DateUtils.nowMillis
    Try(task.run(creds, log)) match {
      case Success(r) => r.map { ret =>
        val msg = s"Completed task [${task.key}] in [${DateUtils.nowMillis - start}ms]."
        log(msg)
        ret
      }.recoverWith {
        case NonFatal(x) =>
          val msg = s"Failed future for [${x.getClass.getSimpleName}] while running task [${task.key}]. ${x.getMessage}"
          log(msg)
          Future.successful(false)
      }
      case Failure(x) =>
        val msg = s"Error [${x.getClass.getSimpleName}] while running task [${task.key}]. ${x.getMessage}"
        log(msg)
        Future.successful(false)
    }
  }
}
