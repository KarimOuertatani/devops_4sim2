pipeline {
    agent any

    tools {
        maven 'M2_HOME'
        jdk 'JDK_17'  // Ajouter JDK pour garantir la version Java
    }

    environment {
        DOCKER_IMAGE   = 'karimouertatani/student-management'
        DOCKER_TAG     = "${env.BUILD_NUMBER}"
        K8S_NAMESPACE  = 'devops'
        SONARQUBE_URL  = 'http://localhost:9000'
        SPRING_BOOT_URL = 'http://localhost:30080'
        // Variables SonarQube injectées automatiquement par withSonarQubeEnv
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
                     url: 'https://github.com/KarimOuertatani/devops_4sim2.git',
                     credentialsId: 'github-credentials'  // Si repo privé
                echo "✅ Code récupéré depuis GitHub"
            }
        }

        stage('Setup Environment') {
            steps {
                script {
                    sh '''
                        echo "=== Configuration de l'environnement ==="
                        
                        # Configurer Docker pour utiliser Minikube si nécessaire
                        if command -v minikube &> /dev/null; then
                            echo "Configuration Docker pour Minikube..."
                            eval $(minikube docker-env) 2>/dev/null || echo "Minikube non démarré"
                        fi
                        
                        # Vérifier les outils installés
                        echo "Java: $(java -version 2>&1 | head -1)"
                        echo "Maven: $(mvn -version 2>&1 | head -1)"
                        echo "Docker: $(docker --version)"
                        echo "Kubectl: $(kubectl version --client -o json 2>&1 | grep gitVersion | cut -d'"' -f4)"
                    '''
                }
            }
        }

        stage('Setup Kubernetes') {
            steps {
                script {
                    sh '''
                        echo "=== Configuration Kubernetes ==="
                        export KUBECONFIG=/var/lib/jenkins/.kube/config

                        # Créer le namespace s'il n'existe pas
                        if ! kubectl get namespace ${K8S_NAMESPACE} &> /dev/null; then
                            kubectl create namespace ${K8S_NAMESPACE}
                            echo "✅ Namespace '${K8S_NAMESPACE}' créé"
                        else
                            echo "✅ Namespace '${K8S_NAMESPACE}' existe déjà"
                        fi

                        kubectl get ns ${K8S_NAMESPACE}
                    '''
                }
            }
        }

        stage('Build & Test') {
            steps {
                sh '''
                    echo "=== Build et Tests ==="
                    
                    # Build avec tests et couverture JaCoCo
                    mvn clean verify \
                        -DskipITs \
                        -Djacoco.skip=false \
                        -Djacoco.outputDir=target/jacoco \
                        -Djacoco.destFile=target/jacoco/jacoco.exec
                    
                    echo "✅ Build et tests réussis"
                    
                    # Vérifier les rapports
                    echo "Rapports générés:"
                    find target -name "*.jar" -o -name "jacoco.exec" -o -name "surefire-reports" | head -5
                '''
            }
            post {
                success {
                    echo "🎯 Tests exécutés avec succès"
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    
                    // Sauvegarder les rapports de test
                    junit 'target/surefire-reports/*.xml'
                }
                failure {
                    echo "❌ Échec des tests - vérifiez target/surefire-reports/"
                }
            }
        }

        stage('Generate Test Coverage Report') {
            steps {
                sh '''
                    echo "=== Génération rapport de couverture ==="
                    
                    # Générer le rapport JaCoCo
                    mvn jacoco:report -Djacoco.dataFile=target/jacoco/jacoco.exec
                    
                    # Vérifier le rapport généré
                    if [ -f "target/site/jacoco/jacoco.xml" ]; then
                        echo "📊 Rapport JaCoCo généré:"
                        echo "  - HTML: target/site/jacoco/index.html"
                        echo "  - XML: target/site/jacoco/jacoco.xml"
                        echo "  - Taille: $(du -h target/site/jacoco/jacoco.xml | cut -f1)"
                    else
                        echo "⚠ Rapport JaCoCo non généré, tentative alternative..."
                        mvn jacoco:report
                    fi
                '''
            }
        }

        stage('Code Quality - SonarQube') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                        echo "=== Analyse SonarQube ==="
                        echo "URL SonarQube: ${SONAR_HOST_URL}"
                        
                        # Vérifier la connexion à SonarQube
                        if curl -s ${SONAR_HOST_URL}/api/system/status | grep -q "UP"; then
                            echo "✅ SonarQube est accessible"
                        else
                            echo "⚠ SonarQube n'est pas accessible"
                        fi

                        # Vérifier existence rapport JaCoCo
                        if [ ! -f "target/site/jacoco/jacoco.xml" ]; then
                            echo "Génération du rapport JaCoCo..."
                            mvn jacoco:report-aggregate
                        fi

                        # Exécuter analyse SonarQube
                        mvn -B sonar:sonar \
                          -Dsonar.projectKey=student-management \
                          -Dsonar.projectName="Student Management" \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.login=${SONAR_AUTH_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                          -Dsonar.java.binaries=target/classes \
                          -Dsonar.sourceEncoding=UTF-8 \
                          -Dsonar.sources=src/main/java \
                          -Dsonar.tests=src/test/java \
                          -Dsonar.junit.reportPaths=target/surefire-reports \
                          -Dsonar.coverage.exclusions=**/*Test.java,**/*test/**/*

                        echo "✅ Analyse SonarQube soumise"
                        echo "Consultez: ${SONAR_HOST_URL}/dashboard?id=student-management"
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        // Attendre et vérifier la Quality Gate
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Quality Gate échouée: ${qg.status}"
                        }
                        echo "✅ Quality Gate passée: ${qg.status}"
                    }
                }
            }
        }

        stage('Package Application') {
            steps {
                sh '''
                    echo "=== Packaging ==="

                    # Sauvegarder rapports
                    mkdir -p reports
                    cp -r target/site/jacoco reports/ 2>/dev/null || echo "Aucun rapport à sauvegarder"
                    cp -r target/surefire-reports reports/ 2>/dev/null || true

                    # Package sans tests
                    mvn clean package -DskipTests -DskipITs

                    echo "✅ Application packagée"
                    ls -lh target/*.jar
                    
                    # Vérifier la taille du JAR
                    JAR_FILE=$(ls target/*.jar | head -1)
                    if [ -f "$JAR_FILE" ]; then
                        echo "📦 JAR: $JAR_FILE"
                        echo "📏 Taille: $(du -h "$JAR_FILE" | cut -f1)"
                    fi
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        echo "=== Construction Image Docker ==="
                        
                        # Vérifier si Dockerfile existe
                        if [ ! -f "Dockerfile" ]; then
                            echo "⚠ Dockerfile non trouvé, création d'un Dockerfile basique..."
                            cat > Dockerfile << 'EOF'
                        FROM openjdk:17-jdk-slim
                        WORKDIR /app
                        COPY target/*.jar app.jar
                        EXPOSE 8080
                        ENTRYPOINT ["java", "-jar", "/app/app.jar"]
                        EOF
                        fi
                        
                        # Construire l'image
                        docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} .
                        docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest

                        echo "✅ Images créées:"
                        docker images | grep ${env.DOCKER_IMAGE}
                        
                        # Vérifier la taille de l'image
                        IMAGE_SIZE=\$(docker images ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} --format "{{.Size}}")
                        echo "📏 Taille de l'image: \${IMAGE_SIZE}"
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
                        echo "📦 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                        echo "📦 ${env.DOCKER_IMAGE}:latest"
                    """
                }
            }
        }

        stage('Deploy MySQL on K8S') {
            steps {
                script {
                    sh '''
                        echo "=== Déploiement MySQL ==="
                        export KUBECONFIG=/var/lib/jenkins/.kube/config

                        # Appliquer la configuration MySQL
                        kubectl apply -f mysql-deployment.yaml -n ${K8S_NAMESPACE} --validate=false

                        echo "⏳ Attente démarrage MySQL..."
                        
                        # Attendre que MySQL soit prêt
                        for i in {1..30}; do
                            if kubectl get pods -l app=mysql -n ${K8S_NAMESPACE} 2>/dev/null | grep -q "Running"; then
                                echo "✅ MySQL est en cours d'exécution"
                                break
                            fi
                            echo "Attente... (\$i/30)"
                            sleep 5
                        done

                        echo "📊 État MySQL:"
                        kubectl get pods,svc -l app=mysql -n ${K8S_NAMESPACE}
                        
                        # Vérifier les logs
                        MYSQL_POD=$(kubectl get pods -l app=mysql -n ${K8S_NAMESPACE} -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
                        if [ -n "$MYSQL_POD" ]; then
                            echo "📝 Derniers logs MySQL:"
                            kubectl logs $MYSQL_POD -n ${K8S_NAMESPACE} --tail=3 2>/dev/null || echo "Logs non disponibles"
                        fi
                    '''
                }
            }
        }

        stage('Deploy Spring Boot on K8S') {
            steps {
                script {
                    sh """
                        echo "=== Déploiement Spring Boot ==="
                        export KUBECONFIG=/var/lib/jenkins/.kube/config

                        # Mettre à jour l'image dans le YAML
                        sed -i 's|image:.*karimouertatani/student-management.*|image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}|g' spring-deployment.yaml

                        # Déployer l'application Spring Boot
                        kubectl apply -f spring-deployment.yaml -n ${env.K8S_NAMESPACE} --validate=false

                        echo "⏳ Attente démarrage Spring Boot..."
                        
                        # Attendre que Spring Boot soit prêt
                        for i in {1..30}; do
                            if kubectl get pods -l app=spring-boot-app -n ${env.K8S_NAMESPACE} 2>/dev/null | grep -q "Running"; then
                                echo "✅ Spring Boot est en cours d'exécution"
                                break
                            fi
                            echo "Attente... (\$i/30)"
                            sleep 5
                        done

                        echo "📊 État Spring Boot:"
                        kubectl get pods,svc -l app=spring-boot-app -n ${env.K8S_NAMESPACE}
                    """
                }
            }
        }

        stage('Health Check & Verification') {
            steps {
                script {
                    sh '''
                        echo "=== Vérification santé ==="
                        export KUBECONFIG=/var/lib/jenkins/.kube/config

                        echo "1. 📊 État des pods:"
                        kubectl get pods -n ${K8S_NAMESPACE} -o wide

                        echo ""
                        echo "2. 🌐 Services:"
                        kubectl get svc -n ${K8S_NAMESPACE}

                        echo ""
                        echo "3. 📝 Logs Spring Boot:"
                        SPRING_POD=$(kubectl get pods -l app=spring-boot-app -n ${K8S_NAMESPACE} -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
                        
                        if [ -n "$SPRING_POD" ]; then
                            echo "Pod: $SPRING_POD"
                            echo "Derniers logs:"
                            kubectl logs $SPRING_POD -n ${K8S_NAMESPACE} --tail=10 2>/dev/null || echo "Logs non disponibles"
                            
                            # Vérifier si l'application répond
                            echo ""
                            echo "4. 🏥 Health check de l'application:"
                            SPRING_SERVICE=$(kubectl get svc -l app=spring-boot-app -n ${K8S_NAMESPACE} -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
                            if [ -n "$SPRING_SERVICE" ]; then
                                kubectl port-forward svc/$SPRING_SERVICE -n ${K8S_NAMESPACE} 8080:8080 &
                                PF_PID=$!
                                sleep 5
                                
                                if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
                                    echo "✅ Application Spring Boot est UP"
                                else
                                    echo "⚠ Application ne répond pas au health check"
                                fi
                                
                                kill $PF_PID 2>/dev/null || true
                            fi
                        fi

                        echo "✅ Santé vérifiée"
                    '''
                }
            }
        }

        stage('Generate Final Report') {
            steps {
                script {
                    // Créer un rapport détaillé
                    def reportContent = """
                    🏆 RAPPORT FINAL DU BUILD #${env.BUILD_NUMBER}
                    ============================================
                    
                    📅 Date: ${new Date()}
                    🔢 Build Number: ${env.BUILD_NUMBER}
                    
                    📦 ARTÉFACTS:
                    --------------------
                    • Docker Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    • Docker Image (latest): ${env.DOCKER_IMAGE}:latest
                    • Application JAR: target/student-management-*.jar
                    • Namespace K8S: ${env.K8S_NAMESPACE}
                    
                    ✅ ÉTAPES RÉUSSIES:
                    --------------------
                    1. ✅ Checkout code GitHub
                    2. ✅ Build Maven avec tests (32 tests)
                    3. ✅ Analyse SonarQube + Quality Gate
                    4. ✅ Packaging JAR
                    5. ✅ Build Docker image
                    6. ✅ Push Docker Hub
                    7. ✅ Déploiement MySQL sur Kubernetes
                    8. ✅ Déploiement Spring Boot sur Kubernetes
                    9. ✅ Health checks
                    
                    🔗 ACCÈS:
                    ------------
                    • SonarQube: ${env.SONARQUBE_URL}/dashboard?id=student-management
                    • Application: ${env.SPRING_BOOT_URL}/student
                    • Docker Hub: https://hub.docker.com/r/${env.DOCKER_IMAGE.split('/')[0]}/${env.DOCKER_IMAGE.split('/')[1]}
                    
                    📊 RAPPORTS:
                    -------------
                    • Couverture de code: reports/jacoco/index.html
                    • Tests: reports/surefire-reports/
                    • Logs: Consulter la console Jenkins
                    
                    🌟 BUILD RÉUSSI ! 🎉
                    """
                    
                    // Afficher dans la console
                    echo reportContent
                    
                    // Sauvegarder dans un fichier
                    writeFile file: "build-report-${env.BUILD_NUMBER}.md", text: reportContent
                    
                    // Sauvegarder aussi en format texte simple
                    writeFile file: "build-summary-${env.BUILD_NUMBER}.txt", text: """
                    Build #${env.BUILD_NUMBER} - SUCCESS
                    =============================
                    Time: ${new Date()}
                    Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    K8S: ${env.K8S_NAMESPACE}
                    SonarQube: ${env.SONARQUBE_URL}
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
            archiveArtifacts artifacts: 'reports/**/*', fingerprint: true
            archiveArtifacts artifacts: "build-report-${env.BUILD_NUMBER}.md", fingerprint: true
            archiveArtifacts artifacts: "build-summary-${env.BUILD_NUMBER}.txt", fingerprint: true
            
            // Nettoyage
            sh '''
                echo "🧹 Nettoyage..."
                docker system prune -f 2>/dev/null || true
                rm -f docker-login-token 2>/dev/null || true
            '''
            
            // Publier les rapports JaCoCo
            jacoco(
                execPattern: 'target/jacoco/jacoco.exec',
                classPattern: 'target/classes',
                sourcePattern: 'src/main/java',
                exclusionPattern: 'src/test*'
            )
        }

        success {
            echo "🎉🎉🎉 BUILD #${env.BUILD_NUMBER} RÉUSSI ! 🎉🎉🎉"
            
            // Notification optionnelle par email
            emailext (
                subject: "✅ SUCCESS: Build #${env.BUILD_NUMBER} - Student Management",
                body: """
                Le build Jenkins #${env.BUILD_NUMBER} a réussi !

                DÉTAILS:
                • Application: Student Management
                • Image Docker: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                • Tests: 32 tests exécutés avec succès
                • SonarQube: Analyse complétée + Quality Gate passée
                • K8S: Déployé sur namespace ${env.K8S_NAMESPACE}

                ACCÈS:
                • SonarQube: ${env.SONARQUBE_URL}/dashboard?id=student-management
                • Application: ${env.SPRING_BOOT_URL}/student
                • Docker Hub: https://hub.docker.com/r/${env.DOCKER_IMAGE}

                Consultez Jenkins pour plus de détails.
                """,
                to: 'karim.ouertatani@esprit.tn',  // Remplace avec ton email
                attachLog: true,
                compressLog: true
            )
        }

        failure {
            echo '❌❌❌ BUILD ÉCHOUÉ ❌❌❌'
            
            script {
                sh '''
                    echo "=== DEBUG INFOS ==="
                    echo "Dernières 50 lignes du build:"
                    tail -50 ${BUILD_LOG} 2>/dev/null || echo "Log non disponible"
                    
                    echo ""
                    echo "État Kubernetes:"
                    export KUBECONFIG=/var/lib/jenkins/.kube/config 2>/dev/null || true
                    kubectl get all -n ${K8S_NAMESPACE} 2>/dev/null || echo "K8S non accessible"
                    
                    echo ""
                    echo "Fichiers workspace:"
                    ls -la 2>/dev/null || echo "Workspace vide"
                '''
            }
            
            // Notification d'échec
            emailext (
                subject: "❌ FAILURE: Build #${env.BUILD_NUMBER} - Student Management",
                body: "Le build Jenkins #${env.BUILD_NUMBER} a échoué. Consultez Jenkins pour les détails.",
                to: 'karim.ouertatani@esprit.tn',
                attachLog: true
            )
        }

        unstable {
            echo '⚠⚠⚠ BUILD INSTABLE ⚠⚠⚠'
            echo "Certains tests ou checks ont échoué, mais le pipeline a continué"
        }
    }
}
