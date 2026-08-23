pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven'
    }

    environment {
        PROJECT_ID = 'gcp-automation-aug-26'
        REGION = 'us-central1'
        REPOSITORY = 'java-devops-repo'
        IMAGE_NAME = 'java-gcp-devops-app'

        NEXUS_HOST = '10.10.0.4:8082'

        TEST_VM_IP = '10.10.0.2'
        TEST_VM_USER = 'devopsazure96'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube-Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                        -Dsonar.projectKey=java-gcp-devops-app
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                    -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Docker Verify') {
            steps {
                sh '''
                    docker images | grep ${IMAGE_NAME}
                '''
            }
        }

        stage('GAR Login') {
            steps {
                withCredentials([file(
                    credentialsId: 'gcp-artifact-key',
                    variable: 'GCP_KEY'
                )]) {
                    sh '''
                        gcloud auth activate-service-account \
                        --key-file=$GCP_KEY

                        gcloud auth configure-docker \
                        ${REGION}-docker.pkg.dev \
                        --quiet
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            parallel {

                stage('Push to GAR') {
                    steps {
                        sh '''
                            docker tag \
                            ${IMAGE_NAME}:${BUILD_NUMBER} \
                            ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:${BUILD_NUMBER}

                            docker push \
                            ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:${BUILD_NUMBER}
                        '''
                    }
                }

                stage('Push to Nexus') {
                    steps {
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'nexus-credentials',
                                usernameVariable: 'NEXUS_USER',
                                passwordVariable: 'NEXUS_PASSWORD'
                            )
                        ]) {
                            sh '''
                                echo "$NEXUS_PASSWORD" | docker login \
                                ${NEXUS_HOST} \
                                -u "$NEXUS_USER" \
                                --password-stdin

                                docker tag \
                                ${IMAGE_NAME}:${BUILD_NUMBER} \
                                ${NEXUS_HOST}/${IMAGE_NAME}:${BUILD_NUMBER}

                                docker push \
                                ${NEXUS_HOST}/${IMAGE_NAME}:${BUILD_NUMBER}
                            '''
                        }
                    }
                }
            }
        }

        stage('Deploy to Test') {
            steps {
                sh '''
                    ssh -o StrictHostKeyChecking=no \
                    ${TEST_VM_USER}@${TEST_VM_IP} "

                        docker pull \
                        ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:${BUILD_NUMBER} &&

                        docker rm -f java-app-test || true

                        docker run -d \
                        --name java-app-test \
                        -p 8081:8080 \
                        ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:${BUILD_NUMBER}
                    "
                '''
            }
        }

        stage('Verify Test Deployment') {
            steps {
                sh '''
                    sleep 5

                    ssh -o StrictHostKeyChecking=no \
                    ${TEST_VM_USER}@${TEST_VM_IP} "

                        docker ps | grep java-app-test &&

                        curl -f http://localhost:8081
                    "
                '''
            }
        }
    }

    post {

        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
        }
    }
}