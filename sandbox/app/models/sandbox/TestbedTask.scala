package models.sandbox

import com.kyleu.projectile.models.sandbox.SandboxTask
import com.kyleu.projectile.util.CsvUtils
import com.kyleu.projectile.util.tracing.TraceData
import io.circe.Json

import scala.concurrent.Future
import scala.io.Source

object TestbedTask extends SandboxTask("testbed", "Testbed", "Just new boot goofin'") {
  override def call(cfg: SandboxTask.Config)(implicit trace: TraceData) = cfg.argument match {
    case None => Future.successful(Json.fromString("Please upload a file to process"))
    case Some(content) =>
      val csv = CsvUtils.readCsv(Source.fromString(content)).filter(_.nonEmpty)
      if (csv.isEmpty) {
        Future.successful(Json.fromString("No rows available"))
      } else {
        val (cols, data) = csv.head -> csv.tail
        val result = process(cols, data)
        Future.successful(Json.fromString(s"$result: Processed [${csv.size}] rows..."))
      }
  }

  def process(cols: List[String], data: List[List[String]]) = {
    "TODO"
  }
}
