Execution of program
----
System prerequisite:
- Java 8
- MySql Data base
- gradle


Here you will find the program compiled as parser.jar. 

Here we use Spring batch framework to accomplish our goal of loading a really big web server log and analyze its data. 
In this case determine what IP address to block.
  
application.properties file has the program default configuration parameters that are:

connection string. Here it uses a database named "wallethub" with
port 3305 and as you can see if it does not exist it will create the database.
datasource.url=jdbc:mysql://localhost:3305/wallethub?createDatabaseIfNotExist=true

username to connect to mysql
datasource.username=root

password to connect to mysql
datasource.password=mysql

chunk size for batch process
application.job.chunkSize=1000

This program is designed to run and create the whole schema thanks to Liquibase, but in case you want to run it on your own
you can find the schema creation in schema.sql.

The file named queries.sql contains queries for testing


Compile source code
----

If you want to compile the source code type :

        gradle clean heavyJar
