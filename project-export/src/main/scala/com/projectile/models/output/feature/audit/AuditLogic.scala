package com.projectile.models.output.feature.audit

import com.projectile.models.output.feature.FeatureLogic

object AuditLogic extends FeatureLogic {
  override val injections = Seq(InjectAuditLookup, InjectAuditRoutes)
}
