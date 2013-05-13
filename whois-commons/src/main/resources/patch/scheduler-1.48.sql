-- Create new DB for scheduler
--
-- Add properties for update-test for this database (schduler.database.*)
--

CREATE TABLE scheduler (
  date DATE NOT NULL,
  host varchar(50) NOT NULL,
  PRIMARY KEY (date)
);
