/*Added serial and change text of times to bigints*/
/*id serial,*/
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