package com.kyleu.projectile.models.task

import play.api.mvc.Call

trait TaskTool[T] {
  def value: String
  def title: String
  def desc: String
  def tasks: Seq[String] = Nil
  def act: T => Call
  def args: Seq[(String, String, String)] = Nil
  def allowed(t: T): Boolean
}
