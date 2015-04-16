/* Drop the table if it is already there */
drop table if exists clean_flight;

/* Create the clean flight table */
CREATE TABLE "clean_flight"
(
    id serial,
    provider text,
    flight_number text,
    schedualed_departure bigint,
    actual_departure bigint,
    departure_id text,
    schedualed_arrival bigint,
    actual_arrival bigint,
    arrival_id text
);