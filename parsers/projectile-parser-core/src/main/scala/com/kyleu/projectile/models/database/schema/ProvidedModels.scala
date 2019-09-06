package com.kyleu.projectile.models.database.schema

object ProvidedModels {
  val models = Map(
    "audit" -> (("audit", "audit", "Audit")),
    "audit_record" -> (("audit", "auditRecord", "AuditRecord")),
    "feedback" -> (("feedback", "feedback", "Feedback")),
    "flyway_schema_history" -> (("migrate", "migration", "Migration")),
    "note" -> (("note", "note", "Note")),
    "oauth2_info" -> (("auth", "oAuth2Info", "OAuth2Info")),
    "password_info" -> (("auth", "passwordInfo", "PasswordInfo")),
    "scheduled_task_run" -> (("task", "scheduledTaskRun", "ScheduledTaskRun")),
    "system_permission" -> (("permission", "permission", "Permission")),
    "system_user" -> (("user", "systemUser", "SystemUser"))
  )
}
