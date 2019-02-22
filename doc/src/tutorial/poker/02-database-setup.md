# Database Setup

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate.poker), 
and have completed the [previous tutorial step](01-hello-world.md)

@@@

Now that we have a basic project configured, let's add a database schema for Projectile to use for code generation. 

Go ahead and checkout the `tutorial-2` tag by running `git checkout -b tutorial-2`. This adds four files in `./ddl`:

- [create-db.ddl.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/create-db.ddl.sql), containing statements to create the database and user
- [schema.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/schema.sql), describing all of the tables and types needed in the database
- [seed-data.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/seed-data.sql), inserting initial test data, so we have something to look at
- [drop.ddl.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/drop.ddl.sql), which, when run, removes everything created by [schema.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/schema.sql)

Using a tool like `psql` or [Database Flow](https://databaseflow.com), run [create-db.ddl.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/create-db.ddl.sql), or manually create an empty database named `estimate`.
Then, run [schema.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/schema.sql) to create the database structure, and [seed-data.sql](https://github.com/KyleU/estimate.poker/blob/master/ddl/seed-data.sql) to create the initital data.

Connect to your database, and run `select * from session`. If at least one row comes back, you're in business.

Now it's time to configure Projectile, giving it the configuration it needs to generate some code, so @ref[get going](03-adding-projectile.md)!
