#!/bin/bash

psql -U test test01 -c " \
CREATE TABLE member (\
	id INTEGER NOT NULL PRIMARY KEY,\
	first_name VARCHAR(20) NOT NULL,\
	last_name VARCHAR(20) NOT NULL,\
	full_name VARCHAR(40) NOT NULL\
);
"
psql -U test test02 -c " \
CREATE TABLE member (\
	id INTEGER NOT NULL PRIMARY KEY,\
	first_name VARCHAR(20) NOT NULL,\
	last_name VARCHAR(20) NOT NULL,\
	full_name VARCHAR(40) NOT NULL\
);
psql -U test test03 -c " \
CREATE TABLE member (\
	id INTEGER NOT NULL PRIMARY KEY,\
	first_name VARCHAR(20) NOT NULL,\
	last_name VARCHAR(20) NOT NULL,\
	full_name VARCHAR(40) NOT NULL\
);