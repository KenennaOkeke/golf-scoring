CREATE SCHEMA IF NOT EXISTS tournaments;
SET search_path TO tournaments;

create table if not exists plays
(
    id uuid not null
        constraint plays_pk
            primary key,
    tournament_id varchar not null,
    name varchar not null,
    score integer not null,
    hole integer not null
);

alter table plays owner to postgres;

create unique index if not exists play_locking_key
    on plays (tournament_id, name, hole);
