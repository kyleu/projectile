package services

import models.database.input.{PostgresConnection, PostgresInput}
import models.input.{InputSummary, InputTemplate}
import models.output.feature.ProjectFeature
import models.project.member.{EnumMember, ModelMember}
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

  /*
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
  */

  // Project
  it should "create a project" in {
    svc.saveProject(ProjectSummary(template = ProjectTemplate.Custom, key = projectKey, title = "Test Project", features = ProjectFeature.set))
    val p = svc.getProject(projectKey)
    p.key should be(projectKey)
    p.features should be(ProjectFeature.set)
  }

  /*
  // Members
  it should "add members correctly" in {
    val e = svc.saveEnumMember(projectKey, EnumMember(inputKey, EnumMember.InputType.PostgresEnum, "setting_key", Seq("settingKey")))

    val t = svc.saveModelMember(projectKey, ModelMember(inputKey, ModelMember.InputType.PostgresTable, "audit", Seq("audit")))
    val t2 = svc.saveModelMember(projectKey, ModelMember(inputKey, ModelMember.InputType.PostgresTable, "audit_record", Seq("audit")))

    val p = svc.getProject(projectKey)
    p.enums.size should be(1)
    p.models.size should be(2)
  }

  it should "fail to add invalid members" in {
    a[IllegalStateException] should be thrownBy {
      svc.saveModelMember("Bad project", ModelMember(inputKey, ModelMember.InputType.PostgresTable, "audit", Seq("audit")))
    }
    a[IllegalStateException] should be thrownBy {
      svc.saveModelMember(projectKey, ModelMember("Bad input", ModelMember.InputType.PostgresTable, "audit", Seq("audit")))
    }
    a[IllegalStateException] should be thrownBy {
      svc.saveModelMember(projectKey, ModelMember(inputKey, ModelMember.InputType.PostgresTable, "invalid", Seq("audit")))
    }
  }

  // Audit
  it should "audit projects correctly" in {
    val result = svc.auditProject(key = projectKey, verbose = true)
    result.outputMessages.size should be(0)
  }

  // Export
  it should "export projects correctly" in {
    val result = svc.exportProject(key = projectKey, verbose = true)
    result._1.project.key should be(projectKey)
    result._1.fileCount should be(12)
  }
  */

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
