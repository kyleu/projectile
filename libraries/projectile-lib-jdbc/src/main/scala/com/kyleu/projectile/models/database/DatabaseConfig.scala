package com.kyleu.projectile.models.database

object DatabaseConfig {
  def fromConfig(cfg: com.typesafe.config.Config, configPrefix: String) = {
    val sectionName = cfg.getString(configPrefix + ".section")
    val section = configPrefix + "." + sectionName

    def get(k: String) = cfg.getString(section + "." + k)
    DatabaseConfig(
      host = get("host"),
      port = get("port").toInt,
      username = get("username"),
      password = Some(get("password")),
      database = Some(get("database")),
      runMigrations = Option(cfg.getBoolean(section + ".runMigrations"))
    )
  }
}

final case class DatabaseConfig(
    host: String = "localhost",
    port: Int = 5432,
    username: String,
    password: Option[String] = None,
    database: Option[String] = None,
    runMigrations: Option[Boolean] = None
) {
  val url: String = s"jdbc:postgresql://$host:$port/${database.getOrElse("")}?stringtype=unspecified"
}
