sudo systemctl start docker
docker login
docker-compose build
docker-compose push
minikube start
kompose convert --out ./kubernetes_deployment
kubectl apply -f ./kubernetes_deployment
