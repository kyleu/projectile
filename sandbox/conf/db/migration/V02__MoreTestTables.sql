create table big (
    id serial not null primary key,
    t text
);

create table small (
    id serial not null primary key,
    big_id int not null references big,
    t text
);
