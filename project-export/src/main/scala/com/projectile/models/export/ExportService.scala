package com.projectile.models.export

import com.projectile.models.feature.ServiceFeature
import com.projectile.models.input.InputType
import com.projectile.models.output.ExportHelper
import com.projectile.models.project.member.ServiceMember
import com.projectile.util.JsonSerializers._

object ExportService {
  implicit val jsonEncoder: Encoder[ExportService] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportService] = deriveDecoder
}

case class ExportService(
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
    throw new IllegalStateException(s"No method for service [$className] with name [$k]. Available methods: [${methods.mkString(", ")}].")
  }
}
