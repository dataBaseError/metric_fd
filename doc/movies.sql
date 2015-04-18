/* Drop the table if it is already there */
drop table if exists movie;

/* Create the table */
CREATE TABLE "movie"
(
    id serial,
    website text,
    name text,
    name_2 text,
    duration integer
);

/* Copy the values into the table */
\copy "movie"(website, name, name_2, duration) from '/path/to/project/doc/MovieRuntime.txt';
