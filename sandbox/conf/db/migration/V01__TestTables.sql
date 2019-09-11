create table top (
    id uuid not null primary key,
    t text
);

create table bottom (
    id uuid not null primary key,
    top_id uuid not null references top,
    t text
);
