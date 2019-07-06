create table if not exists "feedback" (
  "id" uuid primary key,
  "text" text not null,
  "author_id" uuid not null,
  "author_email" text not null,
  "created" timestamp without time zone not null,
  "status" varchar(128) not null,
  foreign key ("author_id") references "system_user" ("id")
);

create index if not exists "feedback_author_id_idx" on "feedback" using btree ("author_id" asc);
create index if not exists "feedback_created_idx" on "feedback" using btree ("created" asc);
