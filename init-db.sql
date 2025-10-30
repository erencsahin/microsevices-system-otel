-- Create databases for each service
CREATE DATABASE userdb;
CREATE DATABASE orderdb;
CREATE DATABASE productdb;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE userdb TO admin;
GRANT ALL PRIVILEGES ON DATABASE orderdb TO admin;
GRANT ALL PRIVILEGES ON DATABASE productdb TO admin;
