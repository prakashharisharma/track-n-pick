@echo on
echo "Starting up the tnp db..."
start /b D:\servers\mongodb\bin\mongod.exe

echo "Starting up the tnp application..."
java -jar tnp.war
