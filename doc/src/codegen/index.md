# Code Generation

@@@ index

* [Database](database.md)
* [Thrift](thrift.md)
* [GraphQL](graphql.md)
* [TypeScript](typescript.md)
* [SBT Plugin](sbt-plugin.md)

@@@

[API Documentation](../api/projectile-export/com/kyleu/projectile/index.html)


#### @ref[Database Export](database.md)

For PostgreSQL databases, Projectile can generate a full admin web application, 
[Slick](http://slick.lightbend.com) and [Doobie](https://tpolecat.github.io/doobie) bindings, 
[GraphQL](http://sangria-graphql.org) and [OpenAPI](https://swagger.io/docs/specification/about) endpoints, and more.


#### @ref[Thrift Export](thrift.md)

For Thrift APIs, Projectile can extend [Scrooge](https://github.com/twitter/scrooge)-generated Scala code 
to provide case classes with Json serialization, services that use Scala Futures, and native scalars.


#### @ref[GraphQL Export](graphql.md)

For a GraphQL schema and a set of queries, Projectile can create strongly-typed Scala representations of your models and services, and helper methods to call a running api. 


#### @ref[TypeScript](typescript.md)

Projectile parses TypeScript definitions and creates a Scala.js facade project 


#### @ref[SBT Plugin](sbt-plugin.md)

An (optional) SBT plugin is provided that updates your generated code on compilation.
