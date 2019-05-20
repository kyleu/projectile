package com.kyleu.projectile.services.notification

import com.kyleu.projectile.models.notification.Notification
import com.kyleu.projectile.models.user.SystemUser

object NotificationService {
  private[this] var callbackOpt: Option[Callback] = None

  type Callback = Option[SystemUser] => Seq[Notification]

  def setCallback(f: Callback) = callbackOpt = Some(f)

  def getNotifications(u: Option[SystemUser]) = callbackOpt.map(_(u)).getOrElse(Seq.empty)
}
