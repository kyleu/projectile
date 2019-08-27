package com.kyleu.projectile.services.task

import java.util.UUID

import akka.actor.ActorSystem
import com.kyleu.projectile.models.config.Configuration
import com.kyleu.projectile.models.task.ScheduledTaskRun
import com.kyleu.projectile.models.task.{ScheduledTask, ScheduledTaskOutput}
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.{DateUtils, Logging}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class ScheduledTaskService @javax.inject.Inject() (
    config: Configuration,
    runService: ScheduledTaskRunService,
    tracingService: TracingService
)(implicit ec: ExecutionContext) extends Logging {
  def initSchedule(system: ActorSystem, creds: Credentials, args: Seq[String], delaySecs: Int = 5, intervalSecs: Int = 15)(implicit td: TraceData): Unit = {
    if (config.scheduledTaskEnabled) {
      import scala.concurrent.duration._
      log.info(s"Scheduling task to run every [$intervalSecs] seconds, after an initial [$delaySecs] second delay.")
      system.scheduler.schedule(delaySecs.seconds, intervalSecs.seconds, (() => runAll(creds, args)): Runnable)
    } else {
      log.info("Scheduled task is disabled, skipping timed schedule")
      log.info("To enable, set [scheduled.task.enabled] in application.conf")
    }
  }

  def runAll(creds: Credentials = Credentials.system, args: Seq[String] = Nil)(implicit td: TraceData) = {
    start(creds, ScheduledTaskRegistry.getAll, args)
  }

  def runSingle(creds: Credentials = Credentials.system, task: ScheduledTask, args: Seq[String] = Nil)(implicit td: TraceData) = {
    start(creds, Seq(task), args)
  }

  private[this] def start(creds: Credentials, tasks: Seq[ScheduledTask], args: Seq[String])(implicit td: TraceData) = {
    tracingService.trace("scheduledTaskService.run") { trace =>
      val id = UUID.randomUUID
      val f = if (tasks.isEmpty) {
        Future.successful(Nil)
      } else {
        log.debug(s"Running scheduled tasks [${tasks.map(_.key).mkString(", ")}].")
        Future.sequence(tasks.toIndexedSeq.map { task =>
          go(id, creds, args, task, trace).map { o =>
            ScheduledTaskRun(id = id, task = task.key, arguments = args.toList, status = o.status, output = o.asJson, started = o.start, completed = o.end)
          }
        })
      }

      f.flatMap { runs =>
        if (args.contains("persist")) {
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

  private[this] def go(id: UUID, creds: Credentials, args: Seq[String], task: ScheduledTask, td: TraceData) = {
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
      log.debug(s"Completed scheduled task [${task.key}] with args [${args.mkString(", ")}] in [${ret.durationMs}ms].")(td)
      ret
    }

    ScheduledTaskRegistry.run(creds, task, addLog)(td).map { ok =>
      addLog(s"Completed scheduled task run [$id].")
      fin(if (ok) { "Ok" } else { "Error" })
    }.recover {
      case NonFatal(x) =>
        addLog(s"Error encountered for scheduled task run [$id].")
        addLog(s" - ${x.getClass.getSimpleName}: ${x.getMessage}")
        x.printStackTrace()
        fin("Error")
    }
  }
}
