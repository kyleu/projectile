# Admin Site

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate) and switched to the `06-web-application` branch

@@@

In this step, we'll be creating an admin site, with a clean UI and performant GraphQL API

We're going to be adding support for Play controllers/routes/views, so a few new files are needed in the project

@@@ warning { title=Disclaimer }

This next part is very boring. We're going to add a bunch of new files that `projectile-lib-auth` expects to be available. 

If you want to skip this section, checkout the git branch [`07-admin-site`](https://github.com/KyleU/estimate/tree/07-admin-site), where these steps have already been performed.

@@@


## Injected files

Some files, like routes or icons, are injected by Projectile, adding references to other generated code. They all work in special comment sessions, see the source for details

- [`app/controllers/admin/system/SearchController.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/controllers/admin/system/SearchController.scala): Handles the admin search pages
- [`app/models/graphql/Schema.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/models/graphql/Schema.scala): Our main Sangria GraphQL schema
- [`app/models/template/Icons.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/models/template/Icons.scala): Font Awesome icons for our models
- [`app/util/web/ModelBindables.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/util/web/ModelBindables.scala): Play Framework routes/query bindables for our enums
- [`app/views/admin/explore/explore.scala.html`](https://github.com/KyleU/estimate/blob/07-admin-site/app/views/admin/explore/explore.scala.html): Twirl template for the admin explore section
- [`conf/system.routes`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/system.routes): Routes for the admin site


## Notes support

With our notes table, we can support arbitrary notes against any of our tables. We'll need to add the following files:

- [`app/controllers/admin/projectile/ModelNoteController.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/controllers/admin/projectile/ModelNoteController.scala): Controller routes to view notes
- [`app/models/queries/note/ModelNoteQueries.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/models/queries/note/ModelNoteQueries.scala): JDBC queries to retrieve, search, and insert notes
- [`app/services/note/ModelNoteService.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/services/note/ModelNoteService.scala): Asynchronous service that retrieves notes for associated models
- [`app/views/admin/note/modelNoteList.scala.html`](https://github.com/KyleU/estimate/blob/07-admin-site/app/views/admin/note/modelNoteList.scala.html): Twirl view for showing a list of notes
- [`app/views/admin/note/notes.scala.html`](https://github.com/KyleU/estimate/blob/07-admin-site/app/views/admin/note/notes.scala.html): Twirl view for showing associated notes
- [`conf/projectile.routes`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/projectile.routes): Routes for note lookups


## More new files

These are utility files for logging and testing, along with a controller and a few views

- [`app/controllers/admin/system/AdminController.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/controllers/admin/system/AdminController.scala): Handles the admin index and explore pages
- [`app/views/admin/layout/menu.scala.html`](https://github.com/KyleU/estimate/blob/07-admin-site/app/views/admin/layout/menu.scala.html): Twirl template for the admin navbar menu
- [`app/views/components/includeScalaJs.scala.html`](https://github.com/KyleU/estimate/blob/07-admin-site/app/views/components/includeScalaJs.scala.html): An empty template for now, it's used by generated code
- [`conf/projectile.routes`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/projectile.routes): Routes for n
- [`conf/application.test.conf`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/application.test.conf): Typesafe config for tests
- [`conf/logback.xml`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/logback.xml): Logback configuration
- [`conf/logback-test.xml`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/logback-test.xml): Logback configuration for tests


## A few tweaks

Projectile expects several classes to be available for injection now. 
Update the following file:
 
- [`app/models/ProjectileModule.scala`](https://github.com/KyleU/estimate/blob/07-admin-site/app/models/ProjectileModule.scala): adds NoteService, GraphQLSchema, and the admin menu
- [`conf/routes`](https://github.com/KyleU/estimate/blob/07-admin-site/conf/routes): Wires `projectile.routes`, `system.routes`, and `graphql.routes`


## Exporting new features

Head back to the project detail page in the Projectile UI and add the "Controller", and "Notes" features.

Generate the new code by hitting "Export", and you're in business.


## The result

If all went well (or you checked out the `07-admin-site` branch), you've now got a full admin site. See @ref[database codegen](../../codegen/database.md) for a tour of the features.


## Explore the code

https://github.com/KyleU/estimate/tree/07-admin-site

See this branch's Pull Request for detailed comments on the generated files

https://github.com/KyleU/estimate/pull/7


## Next steps

Ok, we've got an admin site, user logins, a GraphQL API, and more. It's about time to build a real app. Let's @ref[make a Planning Poker app](08-coming-soon.md)!

