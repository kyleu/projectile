# Basic Codegen

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate) and switched to the `03-configuring-projectile` branch

@@@

Now that you've got Projectile configured, go ahead either run "projectile export" from SBT, or click the big "Export" button in the UI. 

With luck, you'll see a screen showing the code that was generated. Return to sbt and compile, it should pick up a few dozen new files. 


## Schema

As a reminder, this is the schema we're working with

![db schema](img/02-01-ddl.png)

Let's look at what was generated.


## Enums

For each Postgres type, an [Enumeratum](https://github.com/lloydmeta/enumeratum) StringEnum is generated, mixing in Circe classes for json serialization

`src/main/scala/models/SessionStatusType.scala`

```scala
/* Generated File */
package models

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class SessionStatusType(override val value: String) extends StringEnumEntry {
  override def toString = value
}

object SessionStatusType extends StringEnum[SessionStatusType] with StringCirceEnum[SessionStatusType] {
  case object Creating extends SessionStatusType("Creating")
  case object Active extends SessionStatusType("Active")
  case object Complete extends SessionStatusType("Complete")

  override val values = findValues
}
```


## Models

For each Postgres table and view, a Scala case class is generated, with a companion object containing Circe json serialization and a helper factory method

Note that the field `status` is correctly typed to an enum, and Java uuids and dates are correctly handled. 
This is accomplished with a few classes from @ref[projectile-lib-core](../../libraries/core.md), which is cross-compiled to the JVM and JavaScript (we'll see why later)

`src/main/scala/models/SessionRow.scala`

```scala
/* Generated File */
package models

import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.Json
import java.time.LocalDateTime
import java.util.UUID

object SessionRow {
  implicit val jsonEncoder: Encoder[SessionRow] = deriveEncoder
  implicit val jsonDecoder: Decoder[SessionRow] = deriveDecoder

  def empty(
    id: UUID = UUID.randomUUID,
    title: String = "",
    slug: String = "",
    choices: List[String] = List.empty,
    options: Json = Json.obj(),
    status: SessionStatusType = SessionStatusType.Creating,
    created: LocalDateTime = DateUtils.now,
    completed: Option[LocalDateTime] = None
  ) = {
    SessionRow(id, title, slug, choices, options, status, created, completed)
  }
}

final case class SessionRow(
    id: UUID,
    title: String,
    slug: String,
    choices: List[String],
    options: Json,
    status: SessionStatusType,
    created: LocalDateTime,
    completed: Option[LocalDateTime]
)
```


## Use what you've built

Let's modify Entrypoint to create a new session and serialize it to json, just because we can

```scala
import com.kyleu.projectile.util.JsonSerializers._
import models.SessionRow

object Entrypoint {
  def main(args: Array[String]): Unit = {
    val session = SessionRow.empty(title = "Hello!")
    println(session.asJson.spaces2)
  }
}
```

Yep, that worked
```
sbt:estimate.poker> run
[info] Running Entrypoint
{
  "id" : "baab15da-a971-425d-b25b-4fd02746798c",
  "title" : "Hello!",
  "slug" : "",
  "choices" : [
  ],
  "options" : {

  },
  "status" : "Creating",
  "created" : "2019-02-22T17:52:43.154545",
  "completed" : null
}
[success] Total time: 1s
```

## Organizing the output

@@@ warning { title=Disclaimer }

This next part is very boring. We're going to be entering package names for each of the enums and models.

Feel free to reset your repo and switch to the [04-basic-codegen](https://github.com/KyleU/estimate/tree/04-basic-codegen) branch, 
where this is already done, then move on to the @ref[next step](05-more-features.md)

@@@


Currently, all of our classes are being generated in a single package.
This is probably fine for our tiny application, but we can clean it up by assigning packages to the exported models and enums.
Head back to the project detail page

![project detail](img/04-01-project-detail.png)

Click the "Bulk Edit" link at the top of the enums. You'll need to add package names, making it look like [this](img/04-02-bulk-edit-enums.png)

Then head back click the "Bulk Edit" link at the top of the models. More package names, make it look like [this](img/04-03-bulk-edit-models.png)

Click "Export" again, and it will create the same case classes, but all organized now.
However, it left the old files behind, so select "Audit" to detect the orphaned files, then "Fix All".

## Explore the code

https://github.com/KyleU/estimate/tree/04-basic-codegen   

See this branch's Pull Request for detailed comments on the generated files

https://github.com/KyleU/estimate/pull/4


## Next steps

Case classes are cool and all, but we're here to interact with a database, so let's @ref[add more features](05-more-features.md)!
