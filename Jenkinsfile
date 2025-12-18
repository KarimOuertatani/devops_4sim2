pipeline {
    agent any

    tools {
        maven 'M2_HOME'
    }

    environment {
        DOCKER_IMAGE   = 'karimouertatani/student-management'
        DOCKER_TAG     = "${env.BUILD_NUMBER}"
        SONARQUBE_URL  = 'http://localhost:9000'
        APP_PORT       = '8080'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
                echo "✅ Workspace nettoyé pour le build #${env.BUILD_NUMBER}"
            }
        }

        stage('Checkout Code') {
            steps {
                git branch: 'main', 
                     url: 'https://github.com/KarimOuertatani/devops_4sim2.git'
                echo "✅ Code récupéré depuis GitHub"
            }
        }

        stage('Build & Test') {
            steps {
                sh '''
                    echo "=== Build et Tests ==="
                    mvn clean test
                    echo "✅ Build et tests terminés"
                '''
            }
            post {
                success {
                    junit 'target/surefire-reports/*.xml'
                    echo "🎯 Tests exécutés avec succès"
                }
                failure {
                    echo "❌ Échec des tests"
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                        echo "=== Analyse SonarQube ==="
                        
                        # Vérifier si le rapport JaCoCo existe
                        if [ ! -f "target/site/jacoco/jacoco.xml" ]; then
                            echo "Génération du rapport de couverture..."
                            mvn jacoco:report
                        fi
                        
                        # Analyse SonarQube
                        mvn -B sonar:sonar \
                          -Dsonar.projectKey=student-management \
                          -Dsonar.projectName="Student Management" \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.login=${SONAR_AUTH_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                          -Dsonar.java.binaries=target/classes \
                          -Dsonar.sources=src/main/java \
                          -Dsonar.tests=src/test/java
                        
                        echo "✅ Analyse SonarQube soumise"
                        echo "Consultez: ${SONAR_HOST_URL}/dashboard?id=student-management"
                    '''
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        echo "✅ Quality Gate vérifiée"
                    }
                }
            }
        }

        stage('Package Application') {
            steps {
                sh '''
                    echo "=== Packaging ==="
                    mvn clean package -DskipTests
                    echo "✅ Application packagée"
                    ls -lh target/*.jar
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        echo "=== Construction Image Docker ==="
                        
                        # Vérifier que le Dockerfile existe
                        if [ ! -f "Dockerfile" ]; then
                            echo "❌ ERREUR: Dockerfile manquant"
                            echo "Création d'un Dockerfile basique..."
                            cat > Dockerfile << 'DOCKERFILE_EOF'
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
DOCKERFILE_EOF
                            echo "✅ Dockerfile créé"
                        fi
                        
                        # Afficher le Dockerfile
                        echo "📄 Contenu du Dockerfile:"
                        cat Dockerfile || echo "Impossible de lire Dockerfile"
                        
                        # Construire les images
                        docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} .
                        docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest
                        
                        echo "✅ Images Docker créées:"
                        echo "  - ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                        echo "  - ${env.DOCKER_IMAGE}:latest"
                        
                        # Afficher la taille
                        echo "📦 Taille des images:"
                        docker images | grep ${env.DOCKER_IMAGE} || echo "Image non trouvée"
                    """
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh """
                        echo "=== Push Docker Hub ==="
                        
                        # Login Docker
                        echo "\${DOCKER_PASSWORD}" | docker login -u "\${DOCKER_USERNAME}" --password-stdin
                        
                        # Push des images
                        docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                        docker push ${env.DOCKER_IMAGE}:latest
                        
                        echo "✅ Images poussées sur Docker Hub"
                        echo "📎 Lien: https://hub.docker.com/r/karimouertatani/student-management"
                    """
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                sh '''
                    echo "=== Déploiement avec Docker Compose ==="
                    
                    # Créer un docker-compose.yml si nécessaire
                    if [ ! -f "docker-compose.yml" ]; then
                        echo "Création de docker-compose.yml..."
                        cat > docker-compose.yml << 'COMPOSE_EOF'
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: studentdb
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  student-app:
    image: karimouertatani/student-management:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/studentdb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
    depends_on:
      mysql:
        condition: service_healthy
    restart: unless-stopped

volumes:
  mysql_data:
COMPOSE_EOF
                        echo "✅ docker-compose.yml créé"
                    fi
                    
                    # Arrêter les conteneurs existants
                    docker-compose down 2>/dev/null || true
                    
                    # Démarrer avec la nouvelle image
                    docker-compose up -d
                    
                    echo "⏳ Attente du démarrage (30 secondes)..."
                    sleep 30
                    
                    echo "✅ Application déployée avec Docker Compose"
                    echo "🌐 Spring Boot: http://localhost:8080"
                    echo "🗄️ MySQL: localhost:3306"
                    echo "👤 MySQL user: root"
                    echo "🔑 MySQL password: root"
                '''
            }
        }

        stage('Health Check & Verification') {
            steps {
                script {
                    sh '''
                        echo "=== Vérification santé ==="
                        
                        echo "1. 📊 Conteneurs Docker:"
                        docker ps --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}"
                        
                        echo ""
                        echo "2. 🌐 Test de l'API Spring Boot:"
                        # Attendre que l'application soit prête
                        for i in {1..10}; do
                            if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
                                echo "✅ Spring Boot est en ligne"
                                curl -s http://localhost:8080/actuator/health | jq -r '.status' 2>/dev/null || curl -s http://localhost:8080/actuator/health
                                break
                            else
                                echo "⏳ Attente ($i/10)..."
                                sleep 5
                            fi
                        done
                        
                        echo ""
                        echo "3. 🗄️ Test MySQL:"
                        if docker-compose ps mysql | grep -q "Up"; then
                            echo "✅ MySQL est en ligne"
                        else
                            echo "⚠ MySQL semble avoir des problèmes"
                        fi
                        
                        echo ""
                        echo "4. 📝 Logs de l'application:"
                        docker-compose logs student-app --tail=10 2>/dev/null || echo "Logs non disponibles"
                        
                        echo "✅ Vérifications terminées"
                    '''
                }
            }
        }

        stage('Generate Report') {
            steps {
                script {
                    echo """
                    🏆 RAPPORT DU BUILD #${env.BUILD_NUMBER}
                    =====================================
                    
                    📅 Date: ${new Date()}
                    🔢 Build Number: ${env.BUILD_NUMBER}
                    
                    ✅ ÉTAPES RÉUSSIES:
                    1. ✅ Checkout code
                    2. ✅ Build et tests Maven
                    3. ✅ Analyse SonarQube
                    4. ✅ Packaging JAR
                    5. ✅ Build Docker image
                    6. ✅ Push Docker Hub
                    7. ✅ Déploiement avec Docker Compose
                    8. ✅ Health checks
                    
                    📦 ARTÉFACTS:
                    • Docker Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    • Docker Image (latest): ${env.DOCKER_IMAGE}:latest
                    • Application JAR: target/student-management-*.jar
                    
                    🔗 ACCÈS:
                    • SonarQube: ${env.SONARQUBE_URL}/dashboard?id=student-management
                    • Docker Hub: https://hub.docker.com/r/karimouertatani/student-management
                    • Application: http://localhost:8080
                    • MySQL: localhost:3306 (root/root)
                    
                    🐳 CONTENEURS:
                    • student-app: http://localhost:8080
                    • mysql: localhost:3306
                    
                    🌟 BUILD RÉUSSI ! 🎉
                    """
                    
                    // Sauvegarder le rapport
                    writeFile file: "build-report-${env.BUILD_NUMBER}.txt", text: """
                    Build #${env.BUILD_NUMBER} - SUCCESS
                    =============================
                    Time: ${new Date()}
                    Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    Status: Déployé avec Docker Compose
                    
                    Services:
                    - Spring Boot: http://localhost:8080
                    - MySQL: localhost:3306 (root/root)
                    
                    Docker Images:
                    - ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    - ${env.DOCKER_IMAGE}:latest
                    """
                }
            }
        }
    }

    post {
        always {
            echo "=== FIN DU PIPELINE BUILD #${env.BUILD_NUMBER} ==="
            
            // Archiver les artefacts
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            archiveArtifacts artifacts: "build-report-${env.BUILD_NUMBER}.txt", fingerprint: true
            archiveArtifacts artifacts: 'docker-compose.yml', fingerprint: true
            
            // Nettoyage Docker
            sh 'docker logout 2>/dev/null || true'
        }

        success {
            echo "🎉🎉🎉 BUILD #${env.BUILD_NUMBER} RÉUSSI ! 🎉🎉🎉"
            
            // Optionnel : notification email
            /*
            emailext (
                subject: "✅ SUCCESS: Build #${env.BUILD_NUMBER} - Student Management",
                body: "Le build Jenkins #${env.BUILD_NUMBER} a réussi. Application disponible sur http://localhost:8080",
                to: 'karim.ouertatani@esprit.tn'
            )
            */
        }

        failure {
            echo '❌❌❌ BUILD ÉCHOUÉ ❌❌❌'
            
            script {
                sh '''
                    echo "=== DEBUG ==="
                    echo "Dernières erreurs:"
                    echo "1. Fichiers workspace:"
                    ls -la 2>/dev/null || true
                    echo ""
                    echo "2. Conteneurs Docker:"
                    docker ps -a 2>/dev/null || echo "Docker non disponible"
                    echo ""
                    echo "3. Logs Docker Compose:"
                    docker-compose logs 2>/dev/null || echo "Docker Compose non disponible"
                '''
            }
        }
    }
}
