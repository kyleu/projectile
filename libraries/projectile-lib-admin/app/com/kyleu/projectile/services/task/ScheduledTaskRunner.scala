package com.kyleu.projectile.services.task

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.task.{ScheduledTask, ScheduledTaskOutput, ScheduledTaskRun}
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.util.{Credentials, DateUtils, Logging}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ScheduledTaskRunner(runService: ScheduledTaskRunService, tracingService: TracingService)(implicit ec: ExecutionContext) extends Logging {
  def start(creds: Credentials, tasks: Seq[ScheduledTask], injector: Injector, args: Seq[String])(implicit td: TraceData) = {
    tracingService.trace("scheduledTaskService.run") { trace =>
      val id = UUID.randomUUID
      val f = if (tasks.isEmpty) {
        Future.successful(Nil)
      } else {
        log.debug(s"Running scheduled tasks [${tasks.map(_.key).mkString(", ")}]")
        Future.sequence(tasks.toIndexedSeq.map { task =>
          go(id, creds, args, task, injector, trace).map { o =>
            ScheduledTaskRun(id = id, task = task.key, arguments = args.toList, status = o.status, output = o.asJson, started = o.start, completed = o.end)
          }
        })
      }

      f.flatMap { runs =>
        if (args.contains("persist") || args.contains("force")) {
          Future.sequence(runs.map { run =>
            runService.insert(creds, run).map(_.get).recover {
              case NonFatal(_) => run.copy(status = "SaveError")
            }
          })
        } else {
          Future.successful(runs)
        }
      }
    }
  }

  private[this] def go(id: UUID, creds: Credentials, args: Seq[String], task: ScheduledTask, injector: Injector, td: TraceData) = {
    val start = DateUtils.now
    val startMs = DateUtils.toMillis(start)
    val logs = collection.mutable.ArrayBuffer.empty[ScheduledTaskOutput.Log]
    def addLog(msg: String) = logs += ScheduledTaskOutput.Log(msg, (DateUtils.nowMillis - startMs).toInt)
    addLog(s"Starting scheduled task run [$id:${task.key}] at [$start]...")
    def fin(status: String) = {
      val ret = ScheduledTaskOutput(
        userId = creds.id, username = creds.name,
        status = status, logs = logs.toIndexedSeq,
        start = start, end = DateUtils.now
      )
      log.debug(s"Completed scheduled task [${task.key}] with args [${args.mkString(", ")}] in [${ret.durationMs}ms]")(td)
      ret
    }
    ScheduledTaskRegistry.run(creds, task, injector, addLog)(td).map { ok =>
      addLog(s"Completed scheduled task run [$id]")
      fin(if (ok) { "Ok" } else { "Error" })
    }.recover {
      case NonFatal(x) =>
        addLog(s"Error encountered for scheduled task run [$id]")
        addLog(s" - ${x.getClass.getSimpleName}: ${x.getMessage}")
        x.printStackTrace()
        fin("Error")
    }
  }
}
