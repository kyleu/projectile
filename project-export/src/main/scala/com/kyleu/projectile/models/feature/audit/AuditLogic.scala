package com.kyleu.projectile.models.feature.audit

import com.kyleu.projectile.models.feature.FeatureLogic

object AuditLogic extends FeatureLogic {
  override val injections = Seq(InjectAuditLookup, InjectAuditRoutes)
}
