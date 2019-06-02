# Database Migrations

@@@ note { .tip title="This is an admin feature" }

It depends on a project that extends `projectile-lib-admin` or enables the `ProjectilePlayProject` plugin

To enable this feature, add `->  /admin/migrate  projectileMigrate.routes` to your `routes` file

@@@


Runs Flyway on application startup, and provides a UI to explore applied migrations

TODO
