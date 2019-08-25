# Creating a Project

@@@ note { .tip title="An assumption" }

We're hoping you've installed Projectile using one of the methods described in @ref[Installing Projectile](installing.md)
@@@

## Starting point

This assumes you've got an existing project that you'd like to add Projectile generated code to.
If you'd prefer to instead start from a brand new project, @ref[install Projectile](../gettingStarted/installing.md) and run `projectile new` in an empty directory

GUI:
Run `projectile server` from your project's directory, and open http://localhost:20000

CLI:
If you're using the @ref[SBT plugin](../codegen/sbt-plugin.md), run `projectile init` from your project's prompt.
If you're using the installed version, run `projectile init` from your project's directory. 


## Creating the Projectile input

Now we need to make an input pointing to your database, Thrift IDL, or GraphQL schema and queries

GUI:
Either click the "Add Input" link on the home page, or open http://localhost:20000/input directly. 
Fill out the form, making sure to configure your database credentials or file locations.
Once complete, you should see all of your models in the input detail (you can also click "Refresh" to manually update them)

CLI:
Run `projectile input-add`, follow the prompts. Once complete, running `projectile input` should show your input in the list 


## Creating the Projectile project

It's time to actually create your project

GUI:
Either click the "Add Project" link on the home page, or open http://localhost:20000/project directly. 
Fill out the form, making sure to select the input we just created.
at this point, you'll be able to configure the models of the system. 
Head to "Edit Features" to see the available options for project features, and select the ones you want.
Explore the project detail page, it allows you to configure output paths, object naming and features, output packages, and more

CLI:
Run `projectile project-add`, follow the prompts. Once complete, running `projectile project` should show your project in the list 


## Exporting

Now we export your new project, creating all sorts of code

GUI:
The "Export" button on the home page will export all projects, or you can individually export a project from its detail page.
A summary is shown with all results, allowing you to see exactly what was changed

CLI:
Run `projectile export`, and hope for the best :) 


## Auditing (optional)

On occasion, you'll remove something from your input. 
Because exporting the project never deletes files, you may need to run Projectile's auditing to clean up the trash.

GUI:
Simply click "Audit" from the home page, and you'll see a report listing orphaned files (and other project checks).
You can click "Fix All" to clean up the orphaned files and apply other cleanups

CLI:
Run `projectile audit`, and you'll see a report listing orphaned files (and other project checks).
You can run `projectile audit --fix` to clean up the orphaned files and apply other cleanups.

