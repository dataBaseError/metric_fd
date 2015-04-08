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

## Database Setup

1. Ensure postgresql is running:

        sudo service postgresql restart

2. Create the database:

        sudo -u postgres createdb movies

3. Open the new connection

        sudo -u postgres psql movies

4. Paste the schema into the database.

5. Load the dataset into the database

        COPY "movie" from '/path/to/database/csv_database.txt';

6. Update the user's password (where `test123` is the new password):

        ALTER USER postgres with password 'test123';