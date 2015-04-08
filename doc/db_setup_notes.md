
1. Ensure postgresql is running:

        sudo service postgresql restart

2. Create the database:

        sudo -u postgres createdb movies

3. Open the new connection

        sudo -u postgres psql movies

4. Paste the schema into the database.

5. Load the dataset into the database

        COPY "movie" from '/home/joseph/source_code/metric_fd/doc/MovieRuntime.txt';

postgres password: z3vvt0aTlkoPb5

6. Update the user's password:

    ALTER USER postgres with password 'z3vvt0aTlkoPb5';