# Database Setup

Now that we have a basic project configured, let's add a database schema for Projectile to use for code generation. 

Go ahead and checkout the `tutorial-2` tag by running `git checkout -b tutorial-2`. This adds four files in `./ddl`:

- [create-db.ddl.sql](TODO), containing statements to create the database and user
- [schema.sql](TODO), describes all of the tables and types needed in the database
- [seed-data.sql](TODO), inserts initial test data, so we have something to look at
- [drop.ddl.sql](TODO), removes everything created by [schema.sql](TODO)

Using a tool like `psql` or [Database Flow](https://databaseflow.com), run [create-db.ddl.sql](TODO), or manually create an empty database named `estimate`.
Then, run [schema.sql](TODO) to create the database structure, and [seed-data.sql](TODO) to create the initital data.

Connect to your database, and run `select * from session`. If at least one row comes back, you're in business!

Now it's time to configure Projectile, giving it the configuration it needs to generate some code, so @ref[get going](03_adding-projectile.md)!
