<#
.SYNOPSIS
    Применяет все Kubernetes-конфиги из директории deploy/k8s.
.DESCRIPTION
    Рекурсивно ищет YAML-файлы в поддиректориях и применяет их через kubectl apply -f.
#>

# Базовый путь до k8s конфигураций
$ALL_DIR = "."
$DOCKER_IMAGE_GATEWAY_SERVICE = 'kuraterut/marketplace-apigateway-service'
$DOCKER_IMAGE_AUTH_SERVICE = 'kuraterut/marketplace-auth-service'
$DOCKER_IMAGE_EUREKA_SERVER = 'kuraterut/marketplace-eureka-server'
$DOCKER_IMAGE_ORDER_SERVICE = 'kuraterut/marketplace-order-service'
$DOCKER_IMAGE_PAYMENT_SERVICE = 'kuraterut/marketplace-payment-service'
$DOCKER_IMAGE_PRODUCT_SERVICE = 'kuraterut/marketplace-product-service'

cd ApiGateway
docker build -t $DOCKER_IMAGE_GATEWAY_SERVICE .
cd ../AuthService
docker build -t $DOCKER_IMAGE_AUTH_SERVICE .
cd ../EurekaServer
docker build -t $DOCKER_IMAGE_EUREKA_SERVER .
cd ../OrderService
docker build -t $DOCKER_IMAGE_ORDER_SERVICE .
cd ../PaymentService
docker build -t $DOCKER_IMAGE_PAYMENT_SERVICE .
cd ../ProductService
docker build -t $DOCKER_IMAGE_PRODUCT_SERVICE .
cd ..

echo "H541xm76_2005" | docker login -u "kuraterut" --password-stdin
docker push $DOCKER_IMAGE_GATEWAY_SERVICE
docker push $DOCKER_IMAGE_AUTH_SERVICE
docker push $DOCKER_IMAGE_EUREKA_SERVER
docker push $DOCKER_IMAGE_ORDER_SERVICE
docker push $DOCKER_IMAGE_PAYMENT_SERVICE
docker push $DOCKER_IMAGE_PRODUCT_SERVICE