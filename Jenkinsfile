pipeline {
    agent any

    environment {
        DOCKER_IMAGE_GATEWAY_SERVICE = 'kuraterut/marketplace-apigateway-service'
        DOCKER_IMAGE_AUTH_SERVICE = 'kuraterut/marketplace-auth-service'
        DOCKER_IMAGE_EUREKA_SERVER = 'kuraterut/marketplace-eureka-server'
        DOCKER_IMAGE_ORDER_SERVICE = 'kuraterut/marketplace-order-service'
        DOCKER_IMAGE_PAYMENT_SERVICE = 'kuraterut/marketplace-payment-service'
        DOCKER_IMAGE_PRODUCT_SERVICE = 'kuraterut/marketplace-product-service'
        DOCKER_CREDENTIALS_ID = 'docker-hub'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

//         stage('Test') {
//             steps {
//                 sh 'mvn clean test'
//             }
//         }

        stage('Build Docker Images') {
            when {
                branch 'main'
            }
            steps {
                script {
                    sh '''
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
                    '''
                }
            }
        }

        stage('Push to Docker Hub') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKER_IMAGE_GATEWAY_SERVICE
                        docker push $DOCKER_IMAGE_AUTH_SERVICE
                        docker push $DOCKER_IMAGE_EUREKA_SERVER
                        docker push $DOCKER_IMAGE_ORDER_SERVICE
                        docker push $DOCKER_IMAGE_PAYMENT_SERVICE
                        docker push $DOCKER_IMAGE_PRODUCT_SERVICE
                    '''
                }
            }
        }
    }
}
