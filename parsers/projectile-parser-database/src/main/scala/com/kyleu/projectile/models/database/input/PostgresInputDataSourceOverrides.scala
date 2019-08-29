package com.kyleu.projectile.models.database.input

import javax.sql.DataSource

object PostgresInputDataSourceOverrides {
  private[this] val overrides = collection.mutable.HashMap.empty[String, () => DataSource]

  def set(project: String, ds: () => DataSource) = project.trim match {
    case p if p.isEmpty => throw new IllegalStateException("Specify a project")
    case p => overrides(p) = ds
  }
  def setGlobal(ds: () => DataSource) = overrides("") = ds

  def get(project: String) = overrides.get(project).orElse(overrides.get(""))
}
