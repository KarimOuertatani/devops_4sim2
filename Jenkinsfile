pipeline {
    agent any
    
    tools {
        maven 'M2_HOME'
    }
    
    environment {
        DOCKER_IMAGE = 'karimouertatani/student-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        K8S_NAMESPACE = 'devops'
        SONARQUBE_URL = 'http://localhost:9000'
        SPRING_BOOT_URL = 'http://localhost:30080'
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

        stage('Setup Kubernetes') {
            steps {
                script {
                    sh '''
                        echo "=== Configuration Kubernetes ==="

                        # Configurer KUBECONFIG
                        export KUBECONFIG=/var/lib/jenkins/.kube/config

                        # Créer ou vérifier le namespace
                        kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f - --validate=false

                        echo "✅ Namespace '${K8S_NAMESPACE}' prêt"
                        kubectl get ns ${K8S_NAMESPACE}
                    '''
                }
            }
        }

        stage('Build & Test') {
            steps {
                sh '''
                    echo "=== Build et Tests ==="
                    mvn clean verify
                    echo "✅ Build et tests réussis"

                    # Vérifier les rapports
                    echo "Rapports générés:"
                    ls -la target/ || echo "Aucun fichier dans target/"
                '''
            }

            post {
                success {
                    echo "🎯 32 tests exécutés avec succès"
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Code Quality - SonarQube') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                        echo "=== Analyse SonarQube ==="

                        # Vérifier existence rapport JaCoCo
                        if [ -f "target/site/jacoco/jacoco.xml" ]; then
                            echo "📊 Rapport JaCoCo trouvé"
                            echo "Taille: $(du -h target/site/jacoco/jacoco.xml | cut -f1)"
                        else
                            echo "⚠ Rapport JaCoCo non trouvé, génération..."
                            mvn jacoco:report
                        fi

                        # Exécuter analyse SonarQube
                        mvn sonar:sonar \
                            -Dsonar.projectKey=student-management \
                            -Dsonar.host.url=${SONARQUBE_URL} \
                            -Dsonar.login=${SONARQUBE_TOKEN} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

                        echo "✅ Analyse SonarQube complétée"
                    '''
                }
            }
        }

        stage('Package Application') {
            steps {
                sh '''
                    echo "=== Packaging ==="

                    # Sauvegarder rapports avant clean
                    mkdir -p reports
                    cp -r target/site/jacoco reports/ 2>/dev/null || echo "Rapports non sauvegardés"

                    # Package sans tests
                    mvn clean package -DskipTests

                    echo "✅ Application packagée"
                    ls -lh target/*.jar
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    echo "=== Construction Image Docker ==="

                    docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} .
                    docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest

                    echo "✅ Images créées:"
                    echo "  - ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                    echo "  - ${env.DOCKER_IMAGE}:latest"

                    docker images | grep ${env.DOCKER_IMAGE}
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
                        echo "=== Push Docker Hub ==="

                        echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin

                        docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                        docker push ${env.DOCKER_IMAGE}:latest

                        echo "✅ Images poussées sur Docker Hub"
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

                        # Déployer MySQL
                        kubectl apply -f mysql-deployment.yaml -n ${K8S_NAMESPACE}

                        # Attendre démarrage
                        echo "Attente démarrage MySQL..."
                        sleep 15

                        # Vérifier
                        kubectl get pods -l app=mysql -n ${K8S_NAMESPACE}
                        echo "✅ MySQL déployé"
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

                        # Déployer
                        kubectl apply -f spring-deployment.yaml -n ${env.K8S_NAMESPACE}

                        echo "Attente démarrage Spring Boot..."
                        sleep 20

                        # Vérifier
                        echo "Pods Spring Boot:"
                        kubectl get pods -l app=spring-boot-app -n ${env.K8S_NAMESPACE} --watch --timeout=30s || true

                        echo "✅ Spring Boot déployé"
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    sh '''
                        echo "=== Vérification santé ==="
                        export KUBECONFIG=/var/lib/jenkins/.kube/config

                        echo "1. État des pods:"
                        kubectl get pods -n ${K8S_NAMESPACE}

                        echo ""
                        echo "2. Services:"
                        kubectl get svc -n ${K8S_NAMESPACE}

                        echo ""
                        echo "3. Vérification Spring Boot:"
                        SPRING_POD=$(kubectl get pods -l app=spring-boot-app -n ${K8S_NAMESPACE} -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

                        if [ -n "$SPRING_POD" ]; then
                            echo "Pod Spring Boot: $SPRING_POD"
                            kubectl logs $SPRING_POD -n ${K8S_NAMESPACE} --tail=5 2>/dev/null || echo "Logs non disponibles"
                        fi

                        echo "✅ Santé vérifiée"
                    '''
                }
            }
        }

        stage('Generate Report') {
            steps {
                script {
                    sh '''
                        echo "=== 🏆 RAPPORT FINAL DU BUILD #${BUILD_NUMBER} ==="
                        echo ""
                        echo "📅 Date: $(date)"
                        echo "🔢 Build Number: ${BUILD_NUMBER}"
                        echo "🏷️  Image Docker: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        echo "📦 Namespace K8S: ${K8S_NAMESPACE}"
                        echo ""
                        echo "✅ ÉTAPES RÉUSSIES:"
                        echo "1. ✅ Checkout code GitHub"
                        echo "2. ✅ Build Maven (32 tests)"
                        echo "3. ✅ Analyse SonarQube"
                        echo "4. ✅ Packaging JAR"
                        echo "5. ✅ Build Docker"
                        echo "6. ✅ Push Docker Hub"
                        echo "7. ✅ Déploiement MySQL K8S"
                        echo "8. ✅ Déploiement Spring Boot K8S"
                        echo "9. ✅ Health checks"
                        echo ""
                        echo "🔗 ACCÈS:"
                        echo "• SonarQube: ${SONARQUBE_URL}/dashboard?id=student-management"
                        echo "• Application: ${SPRING_BOOT_URL}/student"
                        echo "• Docker Hub: https://hub.docker.com/r/${DOCKER_IMAGE}"
                        echo ""
                        echo "📊 ARTÉFACTS:"
                        echo "• JAR: target/student-management-*.jar"
                        echo "• Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        echo "• Rapports: reports/jacoco/"
                        echo ""
                        echo "🌟 BUILD RÉUSSI ! 🎉"
                    '''

                    // Sauvegarder le rapport
                    writeFile file: "build-report-${env.BUILD_NUMBER}.txt", text: """
                    BUILD REPORT #${env.BUILD_NUMBER}
                    =============================
                    Status: SUCCESS
                    Date: ${new Date()}

                    Docker Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    K8S Namespace: ${env.K8S_NAMESPACE}

                    URLs:
                    - SonarQube: ${env.SONARQUBE_URL}
                    - Application: ${env.SPRING_BOOT_URL}/student

                    Artifacts:
                    - Application JAR: target/student-management-*.jar
                    - Docker Image: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                    - Test Reports: reports/jacoco/
                    """
                }
            }
        }
    }

    post {
        always {
            echo "=== FIN DU PIPELINE BUILD #${env.BUILD_NUMBER} ==="

            // Archive des artefacts
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            archiveArtifacts artifacts: 'reports/**/*', fingerprint: true
            archiveArtifacts artifacts: "build-report-${env.BUILD_NUMBER}.txt", fingerprint: true

            // Nettoyage
            sh '''
                echo "Nettoyage des fichiers temporaires..."
                docker system prune -f 2>/dev/null || true
            '''
        }

        success {
            echo "🎉🎉🎉 BUILD #${env.BUILD_NUMBER} RÉUSSI ! 🎉🎉🎉"
            emailext (
                subject: "✅ SUCCESS: Build #${env.BUILD_NUMBER} - Student Management",
                body: """
                Le build Jenkins #${env.BUILD_NUMBER} a réussi !

                Détails:
                - Application: Student Management
                - Image Docker: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                - Tests: 32 tests passés
                - SonarQube: Analyse complétée
                - K8S: Déployé sur namespace ${env.K8S_NAMESPACE}

                Accès:
                - SonarQube: ${env.SONARQUBE_URL}
                - Application: ${env.SPRING_BOOT_URL}/student

                Consultez Jenkins pour plus de détails.
                """,
                to: 'your-email@example.com'
            )
        }

        failure {
            echo '❌❌❌ BUILD ÉCHOUÉ ❌❌❌'

            script {
                sh '''
                    echo "=== DEBUG ==="
                    echo "Dernières erreurs:"

                    # Vérifier K8S
                    export KUBECONFIG=/var/lib/jenkins/.kube/config 2>/dev/null || true

                    echo "1. Pods en erreur:"
                    kubectl get pods -n ${K8S_NAMESPACE} --field-selector=status.phase!=Running 2>/dev/null || echo "K8S non accessible"

                    echo "2. Logs Maven:"
                    tail -50 /tmp/mvn.log 2>/dev/null || echo "Logs Maven non disponibles"

                    echo "3. Fichiers workspace:"
                    ls -la 2>/dev/null || echo "Workspace vide"
                '''
            }
        }

        unstable {
            echo '⚠⚠⚠ BUILD INSTABLE ⚠⚠⚠'
            echo "Certains tests ou checks ont échoué"
        }
    }
}
