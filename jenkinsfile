pipeline {
    agent any
    
    tools {
        maven 'M2_HOME'
    }
    
    environment {
        MAVEN_HOME = "${tool 'M2_HOME'}"
        PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"
        DOCKER_IMAGE = "malekmouelhi7/student-management"
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} -f- . << EOF
FROM eclipse-temurin:17-jre-alpine
COPY target/student-management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
                    
                    docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh """
                        echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                        docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                        docker push ${env.DOCKER_IMAGE}:latest
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Build ${env.BUILD_NUMBER} réussi!"
            echo "Image Docker: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
        }
        failure {
            echo 'Build échoué!'
        }
    }
}
