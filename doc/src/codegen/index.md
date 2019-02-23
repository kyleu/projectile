# Code Generation

@@@ index

* [Database](database.md)
* [Thrift](thrift.md)
* [GraphQL](graphql.md)
* [SBT Plugin](sbt-plugin.md)

@@@

[API Documentation](../api/projectile-export/com/kyleu/projectile/index.html)

#### @ref[Database Export](database.md)

For PostgreSQL databases, Projectile can manage a full admin web application, 
[Slick](http://slick.lightbend.com) and [Doobie](https://tpolecat.github.io/doobie) queries, 
GraphQL and OpenAPI endpoints, and more.

#### @ref[Thrift Export](thrift.md)

For Thrift APIs, Projectile can extend [Scrooge](https://github.com/twitter/scrooge)-generated Scala code 
to provide case classes with Json serialization, services that use Scala Futures, and native scalars.

#### @ref[GraphQL Export](graphql.md)

For a GraphQL schema and a set of queries, Projectile can create strongly-typed Scala representations of your models and services, and helper methods to call a running api. 

#### @ref[SBT Plugin](sbt-plugin.md)

An SBT plugin is provided that updates your generated code on compilation.
