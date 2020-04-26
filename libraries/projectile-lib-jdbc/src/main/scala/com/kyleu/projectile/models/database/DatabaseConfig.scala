package com.kyleu.projectile.models.database

import scala.util.control.NonFatal

object DatabaseConfig {
  val defaultDriverClassname = "org.postgresql.Driver"

  def fromConfig(cfg: com.typesafe.config.Config, configPrefix: String) = {
    val sectionName = cfg.getString(configPrefix + ".section")
    val section = configPrefix + "." + sectionName

    def get(k: String) = cfg.getString(section + "." + k)
    def getOptional(k: String) = try {
      Some(get(k))
    } catch {
      case NonFatal(x) => None
    }

    DatabaseConfig(
      driver = getOptional("driver").getOrElse(defaultDriverClassname),
      urlOverride = getOptional("url"),
      host = get("host"),
      port = get("port").toInt,
      username = get("username"),
      password = getOptional("password"),
      database = Some(get("database")),
      runMigrations = try {
        cfg.getBoolean(section + ".runMigrations")
      } catch {
        case NonFatal(x) => false
      }
    )
  }
}

final case class DatabaseConfig(
    driver: String = DatabaseConfig.defaultDriverClassname,
    urlOverride: Option[String] = None,
    host: String = "localhost",
    port: Int = 5432,
    username: String,
    password: Option[String] = None,
    database: Option[String] = None,
    runMigrations: Boolean = false
) {
  val url: String = urlOverride.getOrElse(s"jdbc:postgresql://$host:$port/${database.getOrElse("")}?stringtype=unspecified")
}
