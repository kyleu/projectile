# Adding New Enums

If you have a database project, you'll need to refresh the database after new types are added. 
To refresh, run `projectile refresh` or use the GUI.  

If you have a Thrift or GraphQL project, the available types will be detected automatically.

Once you've added a new enum type, you can run `projectile update` (or click the update button on the project's detail page).
It will display the newly added enums, now you may want to @ref[change the enum's package](package.md).
