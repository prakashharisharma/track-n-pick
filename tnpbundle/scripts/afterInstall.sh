#!/bin/bash
#ip=$(hostname -I)
#if [ $ip == 13.233.68.234 ]
#then
	#sudo kill -9 $(sudo lsof -t -i:8080)
        screen -ls | awk -vFS='\t|[.]' '/tnp-web/ {system("screen -S "$2" -X quit")}'
	screen -d -m -S tnp-web
	#screen -S tnp-web -X stuff "sudo java -Dspring.profiles.active=feature  -jar /home/ubuntu/jenkins/build/libs/precis-app.jar\n"
	screen -S tnp-web -X stuff "sudo java -jar /opt/tnp/boot/tnp.war\n"
#else
#        echo "None of the condition met"
#fi
