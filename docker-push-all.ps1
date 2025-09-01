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
$DOCKER_IMAGE_ORDER_SERVICE = 'kuraterut/marketplace-order-service'
$DOCKER_IMAGE_PAYMENT_SERVICE = 'kuraterut/marketplace-payment-service'
$DOCKER_IMAGE_PRODUCT_SERVICE = 'kuraterut/marketplace-product-service'
$DOCKER_IMAGE_ANALYTICS_SERVICE = 'kuraterut/marketplace-analytics-service'

cd ApiGateway
docker build -t $DOCKER_IMAGE_GATEWAY_SERVICE .
cd ../AuthService
docker build -t $DOCKER_IMAGE_AUTH_SERVICE .
cd ../OrderService
docker build -t $DOCKER_IMAGE_ORDER_SERVICE .
cd ../PaymentService
docker build -t $DOCKER_IMAGE_PAYMENT_SERVICE .
cd ../ProductService
docker build -t $DOCKER_IMAGE_PRODUCT_SERVICE .
cd ../AnalyticsService
docker build -t $DOCKER_IMAGE_ANALYTICS_SERVICE .
cd ..

echo "$env:DOCKERHUB_PASSWORD" | docker login -u "$env:DOCKERHUB_USERNAME" --password-stdin
docker push $DOCKER_IMAGE_GATEWAY_SERVICE
docker push $DOCKER_IMAGE_AUTH_SERVICE
docker push $DOCKER_IMAGE_ORDER_SERVICE
docker push $DOCKER_IMAGE_PAYMENT_SERVICE
docker push $DOCKER_IMAGE_PRODUCT_SERVICE
docker push $DOCKER_IMAGE_ANALYTICS_SERVICE