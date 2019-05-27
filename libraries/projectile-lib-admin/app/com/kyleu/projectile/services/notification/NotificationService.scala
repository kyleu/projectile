package com.kyleu.projectile.services.notification

import com.kyleu.projectile.models.notification.Notification
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.util.tracing.TraceData

object NotificationService {
  type Callback = (Option[SystemUser], TraceData) => Seq[Notification]

  private[this] var callbackOpt: Option[Callback] = None

  def setCallback(f: Callback) = callbackOpt = Some(f)

  def getNotifications(u: Option[SystemUser])(implicit td: TraceData) = callbackOpt.map(_(u, td)).getOrElse(Nil)
}
