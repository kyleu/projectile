package com.kyleu.projectile.util

import java.io.ByteArrayOutputStream

import com.kyleu.projectile.models.database.DatabaseField
import com.kyleu.projectile.util.tracing.TraceData
import com.github.tototoshi.csv.CSVWriter

object CsvUtils {
  def csvFor(name: Option[String], totalCount: Int, records: Seq[Product], fields: Seq[DatabaseField])(trace: TraceData) = {
    val os = new ByteArrayOutputStream()
    val writer = CSVWriter.open(os)

    writer.writeRow(fields.map(_.prop))
    records.foreach(r => writer.writeRow(r.productIterator.toSeq.map {
      case o: Option[_] => o.getOrElse(NullUtils.inst)
      case x => x
    }))
    name.foreach { n =>
      val amt = NumberUtils.withCommas(records.size)
      val totes = NumberUtils.withCommas(totalCount)
      writer.writeRow(Seq(
        s"# $n export with $amt out of $totes results, generated ${DateUtils.niceDateTime(DateUtils.now)}."
      ))
    }

    new String(os.toByteArray)
  }
}
