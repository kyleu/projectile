package com.kyleu.projectile.models.user

import com.mohiva.play.silhouette.api.Identity

object SystemUserIdentity {
  def from(su: SystemUser) = SystemUserIdentity(su)
}

final case class SystemUserIdentity(user: SystemUser) extends Identity {
  def id = user.id
  def username = user.username
  def email = user.email
  def role = user.role
  def profile = user.profile
}
