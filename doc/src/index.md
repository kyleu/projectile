# Projectile

@@@ index

* [Getting Started](gettingStarted/index.md)
* [Tutorials](tutorial/index.md)
* [Code Generation](codegen/index.md)
* [Libraries](libraries/index.md)
* [Technology](technology.md)
* [Troubleshooting](troubleshooting.md)

@@@

Projectile creates beautiful Scala code from your Postgres database, GraphQL queries, or Thrift definitions.

https://kyleu.com/projectile

https://github.com/KyleU/projectile

## @ref[Code Generation](codegen/index.md)

### @ref[Postgres Database](codegen/database.md)

For each table and view, Projectile can generate:

* Case classes with Circe JSON serialization using Scala representations of all database types (including Enumeratum enums)
* Strongly-typed database queries for all CRUD operations and indexed queries, along with an asynchronous service interface
* Slick or Doobie bindings, supporting all Postgres types and pre-built common queries
* A full-featured Sangria GraphQL interface, supporting batched relationship traversal and a lovely IDE and Schema visualizer
* Play framework controllers and views exposing an Openapi/Swagger endpoint and a beautiful admin interface supporting local or OAuth login


### @ref[Thrift IDL](codegen/thrift.md)

Projectile monitors your Thrift IDL, and works with Scrooge to create wrapper classes that support serialization, tracing, and Scala Futures


### @ref[GraphQL Queries](codegen/graphql.md)

Projectile monitors your GraphQL schema and queries, and generates Scala case classes with input and result serialization 


## License

The code is licensed under [CC0-1.0](https://raw.githubusercontent.com/KyleU/projectile/master/license)

You can basically do whatever you want with the code, no attribution required. Make it your own! 
