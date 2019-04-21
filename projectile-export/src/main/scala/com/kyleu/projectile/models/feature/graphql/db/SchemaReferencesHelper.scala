package com.kyleu.projectile.models.feature.graphql.db

import com.kyleu.projectile.models.export.{ExportModel, ExportModelReference}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object SchemaReferencesHelper {
  def writeFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile, isLast: Boolean) = {
    val hasFk = model.foreignKeys.exists(_.references.size == 1)
    val references = ExportModelReference.transformedReferences(config, model)
    references.foreach { ref =>
      val srcModel = ref.src
      val srcField = ref.tf
      val tgtField = ref.f
      if (srcModel.pkFields.nonEmpty) {
        if (srcModel.pkg != model.pkg) {
          file.addImport(srcModel.graphqlPackage(config), srcModel.className + "Schema")
        }
        file.add("Field(", 1)
        file.add(s"""name = "${ref.r.propertyName}",""")
        file.add(s"""fieldType = ListType(${srcModel.className}Schema.${srcModel.propertyName}Type),""")

        val relationRef = s"${srcModel.className}Schema.${srcModel.propertyName}By${srcField.className}"
        val fetcherRef = relationRef + "Fetcher"
        val v = if (srcField.required) { s"c.value.${tgtField.propertyName}" } else { s"Some(c.value.${tgtField.propertyName})" }

        val call = if (ref.r.notNull) { "deferRelSeq" } else { "deferRelSeqOpt" }
        file.add(s"resolve = c => $fetcherRef.$call(", 1)
        file.add(s"${relationRef}Relation, $v")
        file.add(")", -1)

        val comma = if (references.lastOption.contains(ref) && isLast) { "" } else { "," }
        file.add(")" + comma, -1)
      }
    }
  }
}
