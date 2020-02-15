Instructions to run :
########################################
Configuration Properties
########################################
Open config/config.properties and set below properties

1. #DB URL 
db.url= (DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=OSIBASEDEV3DB1.oneshield.com)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=DBASE3))) 

2. #DB username
db.user= BASE_API

3. #DB password
db.password= BASE_API

4. #Number of Processing threads
process.threads = 32

5. #missing statement
missing.statement = raise

6. #context clause for missing statement
context.clause = exception

########################################
Exclude configuration
########################################
Open config/exclude.json and set below properties

1. add package name to packages json array
2. add function name to functions json array
3. add procedure name to procedures json array

########################################
Run
########################################

1. Run NoRaiseFinder.java either using command prompt.
java -jar NoRaiseFinder.java

Program will few seconds to execute and processing, Please check the output directory once "Processing completed, Please check output directory." appeared on console.