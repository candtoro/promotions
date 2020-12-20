-- http://www.h2database.com/html/datatypes.html
drop table IF EXISTS candidate;

create table candidate (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(250) NOT NULL,
  last_name VARCHAR(250) NOT NULL,
  identification bigint NOT NULL,
  birth_date date ,
  email VARCHAR(50),
  score int,
  is_prospect boolean NOT NULL DEFAULT 'false',
  evaluated boolean NOT NULL DEFAULT 'false',
  UNIQUE KEY unique_identification (identification)
);


insert into candidate (first_name, last_name, identification) values
  ('John', 'Dangote', 123456),
  ('Neiro', 'Diaz', 1234567),
  ('Neiro2', 'Diaz2', 12345672),
  ('Neiro3', 'Diaz3', 12345673),
  ('Neiro4', 'Diaz4', 12345674),
  ('Neiro5', 'Diaz5', 12345675),
  ('Neiro6', 'Diaz6', 12345676),
  ('Neiro7', 'Diaz7', 12345677),
  ('Neiro8', 'Diaz8', 12345678),
  ('Neiro9', 'Diaz9', 12345679),
  ('Neiro10', 'Diaz10', 123456710),
  ('Neiro11', 'Diaz11', 123456711),
  ('Neiro12', 'Diaz12', 123456712),
  ('Neiro13', 'Diaz13', 123456713),
  ('Neiro14', 'Diaz14', 123456714),
  ('Neiro15', 'Diaz15', 123456715),
  ('David', 'Cortez', 123456789);