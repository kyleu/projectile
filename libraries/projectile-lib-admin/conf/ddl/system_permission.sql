create table if not exists "system_permission" (
  "role" varchar(256),
  "pkg" varchar(256),
  "model" varchar(256),
  "action" varchar(256),
  "allow" boolean,
  "created" timestamp without time zone not null default now(),
  "created_by" uuid references "system_user",
  primary key ("role", "pkg", "model", "action")
);

create index if not exists "system_permission_created_by_idx" on "system_permission" using btree ("created_by" asc);
create index if not exists "system_permission_role_idx" on "system_permission" using btree ("role" asc);
create index if not exists "system_permission_role_package_idx" on "system_permission" using btree ("role" asc, "pkg" asc);
create index if not exists "system_permission_role_package_model_idx" on "system_permission" using btree ("role" asc, "pkg" asc, "model" asc);

insert into "system_permission" ("role", "pkg", "model", "action", "allow") values ('admin', '', '', '', true);
insert into "system_permission" ("role", "pkg", "model", "action", "allow") values ('user', '', '', '', false);
