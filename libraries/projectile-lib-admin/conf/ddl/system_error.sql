create table if not exists system_error (
    "id" uuid not null primary key,
    "context" varchar(2048) not null,
    "user_id" uuid references "system_user",
    "cls" varchar(2048) not null,
    "message" text not null,
    "stacktrace" text,
    "occurred" timestamp without time zone not null
);

create index if not exists "system_error_context_idx" on "system_error" using btree ("context" asc);
create index if not exists "system_error_user_id_idx" on "system_error" using btree ("user_id" asc);
create index if not exists "system_error_cls_idx" on "system_error" using btree ("cls" asc);
create index if not exists "system_error_message_idx" on "system_error" using btree ("message" asc);
