package models.output.feature.audit

import models.output.feature.FeatureLogic

object AuditLogic extends FeatureLogic {
  override val injections = Seq(InjectAuditLookup, InjectAuditRoutes)
}
