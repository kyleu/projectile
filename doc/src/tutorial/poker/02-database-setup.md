# Database Setup

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate),
and have completed the @ref[previous tutorial step](01-hello-world.md)

@@@

Now that we have a basic project configured, let's add a database schema for Projectile to use for code generation. 

Go ahead and checkout the `02-database-setup` tag by running `git checkout 02-database-setup`. This adds four files in `./conf/db/migration`:

- [00_CreateDatabase.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/00_CreateDatabase.sql), containing statements to create the database and user
- [00_DropAll.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/00_DropAll.sql), which, when run, removes everything created by [V1__InitialSchema.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/V1__InitialSchema.sql)
- [V1__InitialSchema.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/V1__InitialSchema.sql), describing all of the tables and types needed in the database
- [V2__SeedData.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/V2__SeedData.sql), inserting initial test data, so we have something to look at

## Schema

![db schema](img/02-01-ddl.png)


## Apply the DDL

These are Flyway migrations, but we'll apply them manually for now.

Using a tool like `psql` or [Database Flow](https://databaseflow.com), run [00_CreateDatabase.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/00_CreateDatabase.sql), or manually create a user and empty database named `estimate`.
Then, run [V1__InitialSchema.sql.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/V1__InitialSchema.sql) to create the database structure, 
and [V2__SeedData.sql](https://github.com/KyleU/estimate/blob/master/conf/db/migration/V2__SeedData.sql) to create the initital data.

Connect to your database, and run `select * from session`. If at least one row comes back, you're in business.


## Explore the code

https://github.com/KyleU/estimate/tree/02-database-setup   

See this branch's Pull Request for detailed comments on the database tables

https://github.com/KyleU/estimate/pull/2


## Next steps

Now it's time to configure Projectile, giving it the configuration it needs to generate some code, so @ref[get going](03-configuring-projectile.md)!
