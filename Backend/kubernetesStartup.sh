minikube start
kompose convert
kubectl apply -f user-to-user-service-deployment.yaml,server-deployment.yaml,rabbitmq-server-deployment.yaml,rabbitmq-queues-pod.yaml,moderator-service-deployment.yaml,chat-service-deployment.yaml,server-service.yaml,rabbitmq-server-service.yaml
