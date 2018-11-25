package com.projectile.models.feature.audit

import com.projectile.models.feature.FeatureLogic

object AuditLogic extends FeatureLogic {
  override val injections = Seq(InjectAuditLookup, InjectAuditRoutes)
}
