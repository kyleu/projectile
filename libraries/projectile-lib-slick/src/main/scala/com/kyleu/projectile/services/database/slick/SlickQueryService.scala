package com.kyleu.projectile.services.database.slick

import javax.sql.DataSource
import com.kyleu.projectile.services.database.slick.PostgresProfileEx.api._
import slick.dbio.NoStream
import slick.sql.SqlAction
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import slick.util.DumpInfo

object SlickQueryService {
  val imports = PostgresProfileEx.api
}

class SlickQueryService(key: String, dataSource: DataSource, maxConnections: Int, tracingService: TracingService) {
  private[this] val db = Database.forDataSource(
    ds = dataSource,
    maxConnections = Some(maxConnections),
    executor = AsyncExecutor("slick.pool", maxConnections, 10000)
  )

  private[this] val traceKey = key

  def run[R](name: String = "adhoc.query", debugValues: Seq[(String, Any)] = Nil)(act: SqlAction[R, NoStream, Nothing])(implicit parentTd: TraceData) = {
    tracingService.trace(traceKey + "." + name) { td =>
      td.tag("sql", act.statements.mkString("\n\n"))
      debugValues.foreach(v => td.tag(s"value.${v._1}", v._2.toString))
      db.run(act)
    }
  }

  def runBatch[R](name: String = "batch.query", debugValues: Seq[(String, Any)] = Nil)(act: DBIOAction[R, NoStream, Nothing])(implicit parentTd: TraceData) = {
    tracingService.trace(traceKey + "." + name) { td =>
      // Can't use slick with a sequence and get back the statements yet for some reason. Open issue.
      //td.tag("sql", act.mkString("\n\n"))

      def getDumpInfo(dumpInfo: DumpInfo, tabs: String): String = {
        val dumpInfoName = dumpInfo.name
        val attrInfo = dumpInfo.attrInfo
        val mainInfo = dumpInfo.mainInfo
        val children = dumpInfo.children
        val formattedChildren = children.map(child => getDumpInfo(child._2.getDumpInfo, tabs + "\t"))
        s"""
          |$tabs Name: $dumpInfoName
          |$tabs Attr: $attrInfo
          |$tabs Main: $mainInfo
          |$tabs Children:
          |$formattedChildren
        """.stripMargin.trim
      }

      debugValues.foreach(v => td.tag(s"value.${v._1}", v._2.toString))
      td.tag("sql.dump", getDumpInfo(act.getDumpInfo, ""))
      db.run(act)
    }
  }

  def close() = db.close()
}
