CREATE TABLE reservation (
	name VARCHAR(20) NOT NULL PRIMARY KEY,
	file_name VARCHAR(100) NOT NULL
);
INSERT INTO reservation VALUES ('test01', 'bon_jovi.csv');

CREATE TABLE database (
	name VARCHAR(20) NOT NULL PRIMARY KEY,
	host VARCHAR(20) NOT NULL,
	user_name VARCHAR(20) NOT NULL,
	password VARCHAR(20) NOT NULL
);
INSERT INTO database VALUES('test01', 'localhost', 'test', 'test');
INSERT INTO database VALUES('test02', 'localhost', 'test', 'test');
INSERT INTO database VALUES('test03', 'localhost', 'test', 'test');

