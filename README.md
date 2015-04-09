# Metric FD
Implementation to repair Metric Functional Dependencies

## Software Install

1. Set up Java

		sudo apt-add-repository ppa:webupd8team/java		
		sudo apt-get update
		sudo apt-get install oracle-java8-installer

2. Install postgresql

		sudo apt-get install postgresql-9.3

3. Install JDBC for postgresql found [here](https://jdbc.postgresql.org/download.html)

4. Download the latest version of the driver (a jar file)

5. Move the jar file to the `lib/` folder within the project.

6. Create a new project in eclipse.

		File > New > Java Project

7. Set the location to the root location of the project. Click `Finish`

8. Update the eclipse project's build properties to use include the binary on the build path

		Project > Properties > Java Build Path > Libraries

7. Click 'Add External JARs...' and locate jar file within the lib folder (unless the jar is already there).

8. Click Okay once completed to close the dialog.

## Changing PostgreSQL password

1. Enter the main database

		sudo -u postgres psql postgres

2. Update the user's password (where `test123` is the new password):

	        ALTER USER postgres with password 'test123';

## Database Setup

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