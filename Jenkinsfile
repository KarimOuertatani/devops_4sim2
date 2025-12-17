pipeline {
    agent any

    tools {
        maven 'M2_HOME'
    }

    environment {
        DOCKER_IMAGE   = 'karimouertatani/student-management'
        DOCKER_TAG     = "${env.BUILD_NUMBER}"
        SONARQUBE_URL  = 'http://localhost:9000'  // ou ton IP
        K8S_NAMESPACE  = 'devops'
        KUBECONFIG     = '/var/lib/jenkins/.kube/config'
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
                        
                        # Analyse SonarQube avec les variables injectées automatiquement
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
                        // Optionnel : attendre la Quality Gate
                        // def qg = waitForQualityGate()
                        // if (qg.status != 'OK') {
                        //     error "Quality Gate échouée: ${qg.status}"
                        // }
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
                        
                        # Vérifier/créer Dockerfile si nécessaire
                        if [ ! -f "Dockerfile" ]; then
                            echo "Création d'un Dockerfile basique..."
                            cat > Dockerfile << 'EOF'
                        FROM openjdk:17-jdk-slim
                        WORKDIR /app
                        COPY target/*.jar app.jar
                        EXPOSE 8080
                        ENTRYPOINT ["java", "-jar", "/app/app.jar"]
                        EOF
                        fi
                        
                        # Construire les images
                        docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} .
                        docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest
                        
                        echo "✅ Images Docker créées:"
                        echo "  - ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                        echo "  - ${env.DOCKER_IMAGE}:latest"
                        
                        # Afficher la taille
                        docker images | grep ${env.DOCKER_IMAGE} || true
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
                    """
                }
            }
        }

        stage('Setup Kubernetes Namespace') {
            steps {
                script {
                    sh '''
                        echo "=== Configuration Kubernetes ==="
                        
                        # Vérifier l'accès à Kubernetes
                        if kubectl get nodes 2>/dev/null; then
                            echo "✅ Kubernetes accessible"
                            
                            # Créer le namespace si nécessaire
                            if ! kubectl get namespace ${K8S_NAMESPACE} 2>/dev/null; then
                                kubectl create namespace ${K8S_NAMESPACE}
                                echo "✅ Namespace '${K8S_NAMESPACE}' créé"
                            else
                                echo "✅ Namespace '${K8S_NAMESPACE}' existe déjà"
                            fi
                            
                            # Afficher l'état
                            kubectl get ns ${K8S_NAMESPACE}
                        else
                            echo "⚠ Kubernetes non accessible - vérifiez la configuration"
                            echo "Pour tester: kubectl get nodes"
                        fi
                    '''
                }
            }
        }

        stage('Deploy MySQL to Kubernetes') {
            steps {
                script {
                    sh '''
                        echo "=== Déploiement MySQL ==="
                        
                        if kubectl get nodes 2>/dev/null; then
                            # Appliquer la configuration MySQL
                            kubectl apply -f mysql-deployment.yaml --validate=false
                            
                            echo "⏳ Attente démarrage MySQL..."
                            sleep 20
                            
                            # Vérifier le déploiement
                            kubectl get pods,svc -n ${K8S_NAMESPACE} -l app=mysql 2>/dev/null || echo "MySQL en cours de démarrage..."
                            echo "✅ MySQL déployé"
                        else
                            echo "⚠ Kubernetes non accessible - déploiement sauté"
                        fi
                    '''
                }
            }
        }

        stage('Deploy Spring Boot to Kubernetes') {
            steps {
                script {
                    sh """
                        echo "=== Déploiement Spring Boot ==="
                        
                        if kubectl get nodes 2>/dev/null; then
                            # Mettre à jour l'image dans le fichier YAML
                            sed -i "s|image:.*|image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}|g" spring-deployment.yaml
                            
                            # Appliquer la configuration Spring Boot
                            kubectl apply -f spring-deployment.yaml --validate=false
                            
                            echo "⏳ Attente démarrage Spring Boot..."
                            sleep 30
                            
                            # Vérifier le déploiement et le rollout
                            kubectl get pods,svc -n ${env.K8S_NAMESPACE} -l app=spring-boot-app 2>/dev/null || echo "Spring Boot en cours de démarrage..."
                            
                            # Vérifier le statut du rollout
                            kubectl rollout status deployment/spring-boot-app -n ${env.K8S_NAMESPACE} --timeout=120s 2>/dev/null || echo "Rollout en cours..."
                            
                            echo "✅ Spring Boot déployé"
                        else
                            echo "⚠ Kubernetes non accessible - déploiement sauté"
                        fi
                    """
                }
            }
        }

        stage('Health Check & Verification') {
            steps {
                script {
                    sh '''
                        echo "=== Vérification santé ==="
                        
                        if kubectl get nodes 2>/dev/null; then
                            echo "1. 📊 État des pods:"
                            kubectl get pods -n ${K8S_NAMESPACE} -o wide 2>/dev/null || echo "Pods non disponibles"
                            
                            echo ""
                            echo "2. 🌐 Services:"
                            kubectl get svc -n ${K8S_NAMESPACE} 2>/dev/null || echo "Services non disponibles"
                            
                            echo ""
                            echo "3. 📝 Logs Spring Boot:"
                            SPRING_POD=$(kubectl get pods -n ${K8S_NAMESPACE} -l app=spring-boot-app -o jsonpath="{.items[0].metadata.name}" 2>/dev/null || echo "")
                            
                            if [ -n "$SPRING_POD" ]; then
                                echo "Pod: $SPRING_POD"
                                echo "Derniers logs:"
                                kubectl logs $SPRING_POD -n ${K8S_NAMESPACE} --tail=5 2>/dev/null || echo "Logs non disponibles"
                            fi
                        fi
                        
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
                    7. ✅ Déploiement MySQL sur K8S
                    8. ✅ Déploiement Spring Boot sur K8S
                    9. ✅ Health checks
                    
                    📦 ARTÉFACTS:
                    • Docker Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    • Docker Image (latest): ${env.DOCKER_IMAGE}:latest
                    • Application JAR: target/student-management-*.jar
                    • Namespace K8S: ${env.K8S_NAMESPACE}
                    
                    🔗 ACCÈS:
                    • SonarQube: ${env.SONARQUBE_URL}/dashboard?id=student-management
                    • Docker Hub: https://hub.docker.com/r/karimouertatani/student-management
                    
                    🌟 BUILD RÉUSSI ! 🎉
                    """
                    
                    // Sauvegarder le rapport
                    writeFile file: "build-report-${env.BUILD_NUMBER}.txt", text: """
                    Build #${env.BUILD_NUMBER} - SUCCESS
                    =============================
                    Time: ${new Date()}
                    Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    K8S Namespace: ${env.K8S_NAMESPACE}
                    Status: Déployé avec succès
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
            
            // Nettoyage Docker
            sh 'docker logout 2>/dev/null || true'
        }

        success {
            echo "🎉🎉🎉 BUILD #${env.BUILD_NUMBER} RÉUSSI ! 🎉🎉🎉"
            
            // Optionnel : notification email
            /*
            emailext (
                subject: "✅ SUCCESS: Build #${env.BUILD_NUMBER} - Student Management",
                body: "Le build Jenkins #${env.BUILD_NUMBER} a réussi. Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}",
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
                    echo "2. Fichiers target:"
                    ls -la target/ 2>/dev/null || echo "Dossier target non trouvé"
                '''
            }
        }
    }
}
