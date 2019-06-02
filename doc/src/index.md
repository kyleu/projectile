# Projectile

@@@ index

* [Getting Started](gettingStarted/index.md)
* [Code Generation](codegen/index.md)
* [Admin Web Application](admin/index.md)
* [Libraries](libraries/index.md)
* [Tutorials](tutorial/index.md)
* [Technology](technology.md)

@@@

Projectile creates and manages beautiful Scala code from your Postgres database, GraphQL queries, or Thrift IDL.

With a command line interface or web UI, Projectile lets you extend your project as your API grows and changes.

For Postgres schemata, a full featured web application can be managed and grown, including authentication, GraphQL, Swagger, csv/svg exports, and more.

https://kyleu.com/projectile

https://github.com/KyleU/projectile

## @ref[Code Generation](codegen/index.md)

### @ref[Postgres Database](codegen/database.md)

For each table and view in your schema, Projectile can generate:

* Case classes with Circe @ref[json](codegen/features/db/core.md) serialization using Scala representations of all database types (including Enumeratum enums)
* Strongly-typed queries for all CRUD operations and indexed relationships, along with an asynchronous @ref[service](codegen/features/db/service.md) interface supporting end-to-end tracing
* @ref[Slick](codegen/features/db/slick.md) or @ref[Doobie](codegen/features/db/doobie.md) bindings, supporting all Postgres types and pre-built common queries
* A full-featured Sangria @ref[GraphQL](codegen/features/db/graphql.md) interface, supporting batched relationship traversal and a lovely IDE and Schema visualizer
* Play Framework controllers and views exposing an @ref[OpenAPI](codegen/features/db/openapi.md)/Swagger endpoint 
* A detailed and beautiful @ref[admin](admin/index.md) @ref[web](codegen/features/db/controller.md) application supporting local or OAuth login, and @ref[loads of features](admin/index.md)


### @ref[GraphQL Queries](codegen/graphql.md)

Projectile monitors your GraphQL schema and queries, and generates Scala case classes with input and result serialization.

You can start a new GraphQL project with `projectile new graphql`, or view the @ref[example project](tutorial/starwars/index.md).


### @ref[Thrift IDL](codegen/thrift.md)

Projectile monitors your Thrift IDL and works with Scrooge to create wrapper classes that support serialization, tracing, and Scala Futures

You can start a new GraphQL project with `projectile new thrift`, or view the @ref[example project](tutorial/thrifty/index.md).


## License

The code is licensed under [CC0-1.0](https://raw.githubusercontent.com/KyleU/projectile/master/license)

You can basically do whatever you want with the code, no attribution required. Make it your own! 
