package com.kyleu.projectile.models.feature.doobie

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object DoobieFile {
  private[this] val tq = "\"\"\""

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.doobiePackage, key = model.className + "Doobie")
    val cols = model.fields.map(_.key)
    val quotedCols = cols.map("\"" + _ + "\"").mkString(", ")

    file.addImport(config.applicationPackage ++ model.modelPackage, model.className)
    if (model.pkg.nonEmpty) {
      config.addCommonImport(file, "DoobieQueries")
    }
    config.addCommonImport(file, "DoobieQueryService", "Imports", "_")

    file.add(s"""object ${model.className}Doobie extends DoobieQueries[${model.className}]("${model.key}") {""", 1)

    file.add(s"""override val countFragment = fr${tq}select count(*) from "${model.key}"$tq""")
    file.add(s"""override val selectFragment = fr${tq}select $quotedCols from "${model.key}"$tq""")
    file.add()

    file.add(s"""override val columns = Seq(${cols.map("\"" + _ + "\"").mkString(", ")})""")
    file.add(s"override val searchColumns = Seq(${model.searchFields.map("\"" + _.key + "\"").mkString(", ")})")
    file.add()

    file.add("override def searchFragment(q: String) = {", 1)
    file.add(s"""fr$tq${cols.map("\"" + _ + "\"::text = $q").mkString(" or ")}$tq""")
    file.add("}", -1)

    addQueries(config, file, model)
    addReferences(config, file, model)

    file.add("}", -1)
    file
  }

  private[this] def addQueries(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = {
    model.pkFields.foreach(_.addImport(config = config, file = file, pkg = model.doobiePackage))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        file.addImport(Seq("cats", "data"), "NonEmptyList")

        file.add()
        val colProp = field.propertyName

        val sql = s"""(selectFragment ++ whereAnd(fr"$colProp = $$$colProp"))"""
        file.add(s"def getByPrimaryKey($colProp: ${field.scalaType(config)}) = $sql.query[Option[${model.className}]].unique")

        val seqArgs = s"${colProp}Seq: NonEmptyList[${field.scalaType(config)}]"
        file.add(s"""def getByPrimaryKeySeq($seqArgs) = (selectFragment ++ in(fr"$colProp", ${colProp}Seq)).query[${model.className}].to[Seq]""")
      case fields => // multiple columns
        file.add()
        val colArgs = fields.map(f => f.propertyName + ": " + f.scalaType(config)).mkString(", ")
        val queryArgs = fields.map(f => "o." + f.propertyName + " === " + f.propertyName).mkString(" && ")
        file.add(s"// def getByPrimaryKey($colArgs) = ???")
    }
  }

  private[this] def addReferences(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = if (model.foreignKeys.nonEmpty) {
    model.foreignKeys.foreach { fk =>
      fk.references match {
        case h :: Nil =>
          file.addImport(Seq("cats", "data"), "NonEmptyList")

          val col = model.fields.find(_.key == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          col.addImport(config = config, file = file, pkg = model.doobiePackage)
          val propId = col.propertyName
          val propCls = col.className

          file.add()
          file.add(s"""def countBy$propCls($propId: ${col.scalaType(config)}) = (countFragment ++ whereAnd(fr"$propId = $$$propId")).query[Int].unique""")
          val sql = s"""(selectFragment ++ whereAnd(fr"$propId = $$$propId"))"""
          file.add(s"def getBy$propCls($propId: ${col.scalaType(config)}) = $sql.query[${model.className}].to[Seq]")
          val seqSql = s"""(selectFragment ++ whereAnd(in(fr"$propId", ${propId}Seq)))"""
          file.add(s"def getBy${propCls}Seq(${propId}Seq: NonEmptyList[${col.scalaType(config)}]) = $seqSql.query[${model.className}].to[Seq]")
        case _ => // noop
      }
    }
  }
}
