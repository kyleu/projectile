# Removing Unused Files

## Auditing

On occasion, you'll remove something from your input. 
Because exporting the project never deletes files, you may need to run Projectile's auditing to clean up the trash.

GUI:
Simply click "Audit" from the home page, and you'll see a report listing orphaned files (and other project checks).
You can click "Fix All" to clean up the orphaned files and apply other cleanups

CLI:
Run `projectile audit`, and you'll see a report listing orphaned files (and other project checks).
You can run `projectile audit --fix` to clean up the orphaned files and apply other cleanups.

