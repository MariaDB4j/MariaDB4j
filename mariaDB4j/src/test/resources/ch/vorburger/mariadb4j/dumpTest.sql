use planetexpress;

CREATE TABLE crew(
  name varchar(255) NOT NULL,
  id int PRIMARY KEY AUTO_INCREMENT
);

CREATE TABLE occupation(
  name varchar(255) NOT NULL,
  id int PRIMARY KEY AUTO_INCREMENT,
  crew_id int,
  FOREIGN KEY (crew_id) REFERENCES crew (id)
);


INSERT INTO crew (name) VALUES ('Philip J Fry');
INSERT INTO crew (name) VALUES ('Bender Bending Rodriguez');
INSERT INTO crew (name) VALUES ('John A Zoidberg');
INSERT INTO crew (name) VALUES ('Leela Turanga');

INSERT INTO occupation (name, crew_id) VALUES ('Executive Delivery Boy',1);
INSERT INTO occupation (name, crew_id) VALUES ('Girder-bender' ,2);
INSERT INTO occupation (name, crew_id) VALUES ('MD', 3);
INSERT INTO occupation (name, crew_id) VALUES ('Captain', 4);