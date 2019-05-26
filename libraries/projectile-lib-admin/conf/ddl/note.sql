create table if not exists "note" (
  "id" uuid primary key,
  "rel_type" varchar(128),
  "rel_pk" varchar(256),
  "text" text not null,
  "author" uuid not null,
  "created" timestamp without time zone not null,
  foreign key ("author") references "system_user" ("id")
);

create index if not exists "note_rel_type_idx" on "note" using btree ("rel_type" asc);
create index if not exists "note_rel_pk_idx" on "note" using btree ("rel_pk" asc);
create index if not exists "note_text_idx" on "note" using btree ("text" asc);
create index if not exists "note_author_idx" on "note" using btree ("author" asc);
create index if not exists "note_created_idx" on "note" using btree ("created" asc);
