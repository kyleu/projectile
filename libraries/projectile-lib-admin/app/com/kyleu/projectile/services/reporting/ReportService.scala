package com.kyleu.projectile.services.reporting

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.reporting.{ProjectileReport, ReportResult}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.DateUtils.localDateTimeOrdering
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.ExecutionContext

object ReportService {
  val cacheEnabled = true

  private[this] var reports = Map.empty[String, ProjectileReport]

  def register(report: ProjectileReport*) = reports = reports ++ report.map(r => r.key -> r)

  private[this] val resultCacheSize = 10
  private[this] var resultCache = Map.empty[UUID, ReportResult]

  def getCachedResults = resultCache.values.toSeq.sortBy(_.occurred).reverse
  def getCachedResult(id: UUID, userId: UUID, role: String) = resultCache.get(id).flatMap {
    case r if r.runBy == userId => Some(r)
    case r => Some(r)
  }

  def listReports() = reports.values.toSeq.sortBy(_.title)

  def run(key: String, args: Map[String, String], user: UUID, role: String, injector: Injector)(implicit ec: ExecutionContext, td: TraceData) = {
    val report = reports.getOrElse(key, throw new IllegalStateException(s"No registered report with key [$key]"))

    val allowed = PermissionService.checkPaths(role, report.permissions: _*).forall(_._1)

    if (!allowed) {
      throw new IllegalStateException(s"You are not authorized to run report [${report.key}]")
    }

    val startMs = System.currentTimeMillis
    report.run(user, args, injector).map { data =>
      val dur = (System.currentTimeMillis - startMs).toInt
      val result = ReportResult(report = report, args = args, columns = data._1, rows = data._2, runBy = user, durationMs = dur)
      if (cacheEnabled) {
        resultCache = resultCache + (result.id -> result)
        trimCacheIfNeeded()
      }
      result
    }
  }

  private[this] def trimCacheIfNeeded() = while (resultCache.size > resultCacheSize) {
    resultCache = resultCache - getCachedResults.lastOption.getOrElse(throw new IllegalStateException()).id
  }
}
