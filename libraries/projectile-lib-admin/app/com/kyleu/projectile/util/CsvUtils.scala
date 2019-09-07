package com.kyleu.projectile.util

import java.io.ByteArrayOutputStream

import com.kyleu.projectile.models.database.DatabaseField
import com.kyleu.projectile.util.tracing.TraceData
import com.github.tototoshi.csv.{CSVReader, CSVWriter}

import scala.io.Source

object CsvUtils {
  def readCsv(src: Source) = CSVReader.open(src).all()

  def csvFor(name: Option[String], totalCount: Int, records: Seq[Product], fields: Seq[DatabaseField])(trace: TraceData): String = {
    csvForRows(name, totalCount, records.map(_.productIterator.toSeq.map {
      case o: Option[_] => o.getOrElse(NullUtils.inst)
      case x => x
    }), fields.map(_.prop))(trace)
  }

  def csvForRows(name: Option[String], totalCount: Int, rows: Seq[Seq[Any]], fields: Seq[String])(trace: TraceData): String = {
    val os = new ByteArrayOutputStream()
    val writer = CSVWriter.open(os)

    writer.writeRow(fields)
    rows.foreach(r => writer.writeRow(r.map {
      case o: Option[_] => o.getOrElse(NullUtils.inst)
      case x => x
    }))
    name.foreach { n =>
      val amt = NumberUtils.withCommas(rows.size)
      val totes = NumberUtils.withCommas(totalCount)
      writer.writeRow(Seq(
        s"# $n export with $amt out of $totes results, generated ${DateUtils.niceNow}."
      ))
    }
    trace.annotate("exported")
    new String(os.toByteArray)
  }
}
