# Metric FD
Implementation to repair Metric Functional Dependencies

## Software Install

1. Set up Java

2. Install postgresql

3. Install JDBC for postgresql

		https://jdbc.postgresql.org/download.html

4. Download the latest version of the driver (a jar file)

5. Move the jar file to the `lib/` folder within the project.

6. Update the eclipse project's build properties to use include the binary on the build path

	Project > Properties > Java Build Path > Libraries

7. Click 'Add External JARs...' and locate jar file within the lib folder.

8. Click Okay once completed to close the dialog.

9. Update the user's password (where `test123` is the new password):

        ALTER USER postgres with password 'test123';

## Movies Database Setup

1. Ensure postgresql is running:

        sudo service postgresql restart

2. Create the database:

        sudo -u postgres createdb movies

3. Add the schema the schema into the database.

        sudo -u postgres psql movies < doc/database.sql

4. Load the dataset into the database

        sudo -u postgres psql movies
        COPY "database" from 'doc/csv_database.txt';

## Flight Data Setup

1. Create the database

        sudo -u postgres createdb clean_flight

2. Load the schema

        sudo -u postgres psql clean_flight < doc/clean_flight.sql

3. Load in the values using the script.

        sudo -u postgres psql clean_flight
        COPY "clean_flight" from 'doc/csv_database.txt' WITH copy DATESTYLE

        SET DateStyle TO Sql
        SET DateStyle TO Iso
        set datestyle to DMY; % seemed to work