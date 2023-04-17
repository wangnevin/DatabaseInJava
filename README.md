# DatabaseInJava
A relational database server from scratch in Java

The server can parse the incoming command, which is a simplified version of SQL query language, then perform the specified queries, update the data stored in the database and return an appropriate result to the client.

## Launch the database service 
First go to the folder with ```pom.xml``` file and run the server from the command line using ```mvnw exec:java@server```, then start the client by using ```mvnw exec:java@client```. Now the server is ready to receive queries from client and return responses to client.

## Query Language
The query language supports the following main types of query:

- USE: changes the database against which the following queries will be run
- CREATE: constructs a new database or table (depending on the provided parameters)
- INSERT: adds a new record (row) to an existing table
- SELECT: searches for records that match the given condition
- UPDATE: changes the existing data contained within a table
- ALTER: changes the structure (columns) of an existing table
- DELETE: removes records that match the given condition from an existing table
- DROP: removes a specified table from a database, or removes the entire database
- JOIN: performs an inner join on two tables (returning all permutations of all matching records)

A grammar that fully defines the query language can be found here: [Grammar]()

## Examples:
- Create Database and Table
```
CREATE DATABASE markbook;
USE markbook;
CREATE TABLE marks (name, mark, pass);
INSERT INTO marks VALUES ('Dave', 55, TRUE);
INSERT INTO marks VALUES ('Clive', 20, FALSE);
```
- Query data from table
```
SELECT * FROM marks;
SELECT * FROM marks WHERE name != 'Dave';
SELECT * FROM marks WHERE pass == TRUE;
SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);
```
- Update entries in a table
```
UPDATE marks SET mark = 38 WHERE name == 'Clive';
DELETE FROM marks WHERE name == 'Dave';
```
- Join two tables on given entries
```
JOIN coursework AND marks ON submission AND id;
```
All the keywords are case-insensitive, additional whitespace characters between certain tokens are reduced to single whitespace when parsing. The two statements below are equivalent:
```
SELECT    *  FROM     people  WHERE   Name  ==  'Steve' ;
Select * from people where Name=='Steve';
```
