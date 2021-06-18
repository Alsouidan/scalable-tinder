#rabbitmq-server
chmod +x ./wait_for_it.sh
bash ./wait_for_it.sh -h 10.98.206.252 -p 15672 --strict -- mvn exec:java -D exec.mainClass=MessageQueue.ServicesMQ