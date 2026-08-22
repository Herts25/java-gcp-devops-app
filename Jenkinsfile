
pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven'
    }

    environment {
        PROJECT_ID = 'gcp-automation-aug-26'
        REGION = 'us-central1'
        GAR_REPO = 'java-app-repo'
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

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t java-gcp-devops-app:${BUILD_NUMBER} .'
            }
        }

        stage('Docker Verify') {
            steps {
                sh 'docker images | grep java-gcp-devops-app'
            }
        }

        stage('GAR Login') {
            steps {
                withCredentials([
                    file(credentialsId: 'gcp-service-account-key', variable: 'GCP_KEY')
                ]) {
                    sh '''
                        gcloud auth activate-service-account --key-file=$GCP_KEY
                        gcloud auth configure-docker $REGION-docker.pkg.dev --quiet
                    '''
                }
            }
        }

        stage('Tag Image') {
            steps {
                sh '''
                    docker tag java-gcp-devops-app:${BUILD_NUMBER} \
                    $REGION-docker.pkg.dev/$PROJECT_ID/$GAR_REPO/java-gcp-devops-app:${BUILD_NUMBER}
                '''
            }
        }

        stage('Push to GAR') {
            steps {
                sh '''
                    docker push \
                    $REGION-docker.pkg.dev/$PROJECT_ID/$GAR_REPO/java-gcp-devops-app:${BUILD_NUMBER}
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
