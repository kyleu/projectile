# More Features

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate) and switched to the `04-basic-codegen` branch

@@@

Case classes are cool and all, but we're here to interact with a database, so let's add more features!
We'll be adding generated JDBC queries and asynchronous services.

To start, we'll need to add new @ref[supporting libraries](../../libraries/index.md). Open up `build.sbt` and change the `libraryDependencies` line to:

```scala
libraryDependencies ++= Seq("service", "graphql", "slick").map(x => "com.kyleu" %% s"projectile-lib-$x" % "1.3.0")
```

This will add @ref[projectile-lib-service](../../libraries/service.md), @ref[projectile-lib-graphql](../../libraries/graphql.md), and @ref[projectile-lib-slick](../../libraries/slick.md) to our build.

Now return to the [project detail page](http://localhost:20000/project/estimate), and select "Edit Features". 
To match the libraries we included, let's enable features "Service", "GraphQL", and "Slick". 

Now that those are added, export the project again by either clicking the "Export" button of the project detail page, or running "projectile export" from sbt.
Go ahead and compile your project again. 
Assuming it succeeded (remember to restart sbt after you change the libraryDependencies), you've now got a project with full database functionality and slick bindings!


## Generated Queries

To work with the database tables and views, strongly-typed database queries for searching and common CRUD operations were generated.
See [here](https://github.com/KyleU/estimate/blob/05-more-features/src/main/scala/models/queries/session/SessionRowQueries.scala) for an example of what was generated for `Session`

  
## Generated Services

Basic asynchronous services exposing all of the queries were generated.


## Generated Slick Bindings

If you like functional database interactions, you'll love the [Slick](http://slick.lightbend.com) bindings that were generated.
See [here](https://github.com/KyleU/estimate/blob/05-more-features/src/main/scala/models/table/session/SessionRowTable.scala) for an example of what was generated for `Session`


## Use what you've built

Let's modify Entrypoint to open the database, insert a new Session, and retrieve it, serializing to json.
Before we do, we'll have to add [this file](https://github.com/KyleU/estimate/blob/05-more-features/src/main/resources/application.conf) to `src/main/resources/application.conf`. 
There's a lot to unpack here, see the @ref[library documentation](../../libraries/index.md) for more details of database and slick usage

```scala
import java.util.UUID

import com.kyleu.projectile.services.database.ApplicationDatabase
import com.kyleu.projectile.services.database.slick.ApplicationSlick
import com.kyleu.projectile.services.database.slick.SlickQueryService.imports._
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.typesafe.config.ConfigFactory
import models.queries.session.SessionRowQueries
import models.session.{SessionRow, SessionStatusType}
import models.table.session.SessionRowTable

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Entrypoint {
  def main(args: Array[String]): Unit = {
    val cfg = ConfigFactory.load()
    implicit val td: TraceData = TraceData.noop

    ApplicationDatabase.open(cfg, TracingService.noop)
    ApplicationSlick.open(TracingService.noop)

    val id = UUID.randomUUID
    val title = scala.util.Random.alphanumeric.take(8).mkString
    val newSession = SessionRow.empty(id = id, title = title, slug = title.toLowerCase, status = SessionStatusType.Active)

    val future = ApplicationDatabase.executeF(SessionRowQueries.insert(newSession)).flatMap { _ =>
      ApplicationSlick.slick.run("getSession")(SessionRowTable.query.filter(_.id === id).result.head)
    }

    val session = Await.result(future, 10.seconds)
    println(session.asJson.spaces2)
  }
}
```

Yep, that worked
```
sbt:estimate.poker> run
[info] Running Entrypoint
{
  "id" : "ceb7e9f2-03d7-4b72-95e2-d1ec9fbcbfa9",
  "title" : "gdCvAi7n",
  "slug" : "gdcvai7n",
  "choices" : [
  ],
  "options" : {

  },
  "status" : "Active",
  "created" : "2019-02-23T11:44:03.577966",
  "completed" : null
}
[success] Total time: 1s
```

It doesn't look like much, but Entrypoint now opens a database and Slick context, inserts a new session with JDBC, then uses Slick to retrieve the newly-inserted row and print the json representation.
We also generated dependency-injected services and a GraphQL schema, but those will have to until after the next step, where we configure a [Play Framework](https://www.playframework.com) web application


## Explore the code

https://github.com/KyleU/estimate/tree/05-more-features

See this branch's Pull Request for detailed comments on the modified files

https://github.com/KyleU/estimate/pull/5


## Next steps

A console app isn't that impressive, so let's @ref[make a web application](06-web-application.md)!

