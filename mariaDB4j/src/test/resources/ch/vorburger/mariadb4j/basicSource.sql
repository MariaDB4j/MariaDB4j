use junittest;

CREATE TABLE test(
  id int PRIMARY KEY AUTO_INCREMENT,
  name varchar(255) NOT NULL
);


INSERT INTO test (name) VALUES ('John Doe');
INSERT INTO test (name) VALUES ('Jane Doe');
