package services

import models.database.input.{PostgresConnection, PostgresInput}
import models.input.{InputSummary, InputTemplate}
import models.output.feature.Feature
import models.project.member.ProjectMember
import models.project.{ProjectSummary, ProjectTemplate}
import org.scalatest._
import services.config.ConfigService

class ProjectileServiceTests extends FlatSpec with Matchers {
  val dir = "tmp/test"
  val inputKey = "testInput"
  val projectKey = "testProject"
  val cfg = new ConfigService(dir)

  lazy val svc = new ProjectileService(cfg)
  val db = "boilerplay"
  lazy val conn = PostgresConnection(url = s"jdbc:postgresql://localhost/$db", username = db, password = db, db = db)

  "ProjectileService" should "initialize correctly" in {
    cfg.init()
    svc.toString
  }

  // Input
  it should "fail to load a missing input" in {
    a[IllegalStateException] should be thrownBy {
      svc.getInput("lol-no")
    }
  }

  it should "create an input" in {
    svc.addInput(InputSummary(template = InputTemplate.Postgres, key = inputKey, title = "Test Input"))
    val i = svc.getInput(inputKey)
    i.key should be(inputKey)
  }

  it should "save database configuration" in {
    svc.setPostgresOptions(inputKey, conn)
    val i = svc.getInput(inputKey).asInstanceOf[PostgresInput]
    i.url should be(conn.url)
    i.username should be(conn.username)
  }

  it should "refresh the input successfully" in {
    val x = svc.refreshInput(inputKey)
    x.enums.size should be(1)
    x.tables.size should be(10)
  }

  it should "load the saved input successfully" in {
    val x = svc.getInput(inputKey).asInstanceOf[PostgresInput]
    x.enums.size should be(1)
    x.tables.size should be(10)
  }

  // Project
  it should "create a project" in {
    svc.addProject(ProjectSummary(template = ProjectTemplate.Custom, key = projectKey, title = "Test Project", features = Feature.set))
    val p = svc.getProject(projectKey)
    p.key should be(projectKey)
    p.features should be(Feature.set)
  }

  it should "add members correctly" in {
    val e = svc.saveProjectMember(projectKey, ProjectMember(inputKey, ProjectMember.InputType.PostgresEnum, "setting_key", "settingKey"))
    e.input should be(inputKey)
    e.inputKey should be("setting_key")

    val t = svc.saveProjectMember(projectKey, ProjectMember(inputKey, ProjectMember.InputType.PostgresTable, "audit", "audit"))
    t.input should be(inputKey)
    t.inputKey should be("audit")

    val p = svc.getProject(projectKey)
    p.allMembers.size should be(2)
  }

  it should "fail to add invalid members" in {
    a[IllegalStateException] should be thrownBy {
      svc.saveProjectMember("Bad project", ProjectMember(inputKey, ProjectMember.InputType.PostgresTable, "audit", "audit"))
    }
    a[IllegalStateException] should be thrownBy {
      svc.saveProjectMember(projectKey, ProjectMember("Bad input", ProjectMember.InputType.PostgresTable, "audit", "audit"))
    }
    a[IllegalStateException] should be thrownBy {
      svc.saveProjectMember(projectKey, ProjectMember(inputKey, ProjectMember.InputType.PostgresTable, "invalid", "audit"))
    }
  }

  it should "audit projects correctly" in {
    val result = svc.auditProject(key = projectKey, verbose = true)
    result.project.key should be(projectKey)
    result.fileCount should be(2)
  }

  it should "export projects correctly" in {
    val result = svc.exportProject(key = projectKey, verbose = true)
    result.project.key should be(projectKey)
    result.fileCount should be(2)
  }

  // Cleanup
  it should "remove a project" in {
    svc.removeProject(projectKey)
    a[IllegalStateException] should be thrownBy {
      svc.getProject(projectKey)
    }
  }

  it should "remove an input" in {
    svc.removeInput(inputKey)
    a[IllegalStateException] should be thrownBy {
      svc.getInput(inputKey)
    }
  }
}
