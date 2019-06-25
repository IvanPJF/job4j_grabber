CREATE TABLE IF NOT EXISTS vacancy (
  id serial PRIMARY KEY,
  name text NOT NULL,
  text text NOT NULL,
  link text UNIQUE NOT NULL,
  date timestamp NOT NULL
);