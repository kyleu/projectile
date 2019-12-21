package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.feature.ServiceFeature
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.project.member.ServiceMember
import com.kyleu.projectile.util.JsonSerializers._

object ExportService {
  implicit val jsonEncoder: Encoder[ExportService] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportService] = deriveDecoder
}

final case class ExportService(
    inputType: InputType.Service,
    pkg: List[String] = Nil,
    key: String,
    className: String,
    methods: Seq[ExportMethod],
    features: Set[ServiceFeature] = Set.empty
) {
  def apply(m: ServiceMember) = copy(
    pkg = m.pkg.toList,
    className = m.getOverride("className", ExportHelper.toClassName(ExportHelper.toIdentifier(m.key))),
    methods = methods.filterNot(meth => m.ignored.contains(meth.key)),
    features = m.features
  )

  val propertyName = ExportHelper.toIdentifier(className)

  def getMethodOpt(k: String) = methods.find(f => f.key == k)
  def getMethod(k: String) = getMethodOpt(k).getOrElse {
    throw new IllegalStateException(s"No method for service [$className] with name [$k]. Available methods: [${methods.mkString(", ")}]")
  }

  def perm(act: String) = s"""("${pkg.headOption.getOrElse("system")}", "$className", "$act")"""
}
