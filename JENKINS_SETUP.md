# Guide de Configuration et Test du Pipeline Jenkins

Ce document explique comment configurer et tester le pipeline Jenkins pour le projet Student Management sur Jenkins local (localhost:8080).

## Prérequis

1. **Jenkins installé et démarré** sur `http://localhost:8080`
2. **Java JDK 17** installé sur votre machine
3. **Maven** installé sur votre machine
4. **Git** installé (si vous utilisez un dépôt Git)

## Étape 1 : Installation de Jenkins (si non installé)

### Sur Windows :
```powershell
# Télécharger Jenkins depuis https://www.jenkins.io/download/
# Exécuter le fichier jenkins.msi
# Suivre l'installation et noter le mot de passe initial
```

### Sur Linux/Mac :
```bash
# Installation via package manager ou téléchargement direct
# Démarrer Jenkins : sudo systemctl start jenkins
```

## Étape 2 : Configuration Initiale de Jenkins

1. Ouvrir votre navigateur et accéder à `http://localhost:8080`
2. Entrer le mot de passe initial (trouvé dans `/var/lib/jenkins/secrets/initialAdminPassword` sur Linux, ou affiché lors de l'installation sur Windows)
3. Installer les plugins suggérés (ou plugins personnalisés)
4. Créer un utilisateur administrateur
5. Configurer l'URL de Jenkins : `http://localhost:8080`

## Étape 3 : Installation des Plugins Requis

1. Aller dans **Manage Jenkins** → **Plugins**
2. Installer les plugins suivants (dans l'onglet "Available") :
   - **Pipeline** (déjà inclus généralement)
   - **Git** (pour le contrôle de version)
   - **JUnit** (pour les rapports de tests)
   - **Maven Integration** (optionnel mais recommandé)

## Étape 4 : Configuration des Outils (Maven et JDK)

### 4.1 Configuration de JDK 17

1. Aller dans **Manage Jenkins** → **Global Tool Configuration**
2. Dans la section **JDK**, cliquer sur **Add JDK**
3. Configurer :
   - **Name** : `JDK17`
   - **JAVA_HOME** : Chemin vers votre installation JDK 17
     - Windows : `C:\Program Files\Java\jdk-17` (ou votre chemin)
     - Linux/Mac : `/usr/lib/jvm/java-17-openjdk` (ou votre chemin)
4. Cliquer sur **Save**

### 4.2 Configuration de Maven

1. Dans la même page **Global Tool Configuration**
2. Dans la section **Maven**, cliquer sur **Add Maven**
3. Configurer :
   - **Name** : `Maven`
   - **MAVEN_HOME** : Chemin vers votre installation Maven
     - Windows : `C:\Program Files\Apache\maven` (ou votre chemin)
     - Linux/Mac : `/usr/share/maven` ou `/opt/maven` (ou votre chemin)
4. Cliquer sur **Save**

**Note** : Si vous n'avez pas Maven installé, Jenkins peut l'installer automatiquement :
- Cocher **Install automatically**
- Choisir une version (ex: `3.9.6`)

## Étape 5 : Création d'un Pipeline Job

### Option A : Pipeline depuis un Jenkinsfile (Recommandé)

1. Dans le tableau de bord Jenkins, cliquer sur **New Item**
2. Entrer un nom pour le job (ex: `student-management-pipeline`)
3. Sélectionner **Pipeline**
4. Cliquer sur **OK**

### Configuration du Pipeline :

1. **Description** : Ajouter une description optionnelle
2. **Pipeline Definition** : Sélectionner **Pipeline script from SCM**
3. **SCM** : Sélectionner **Git**
4. **Repository URL** : 
   - Si le projet est sur GitHub/GitLab : `https://github.com/votre-username/Deveops.git`
   - Si le projet est local : Utiliser un chemin de fichier ou configurer un serveur Git local
5. **Credentials** : Ajouter vos identifiants Git si nécessaire
6. **Branch Specifier** : `*/main` ou `*/master` (selon votre branche)
7. **Script Path** : `Jenkinsfile` (le nom du fichier à la racine du projet)
8. Cliquer sur **Save**

### Option B : Pipeline Script Direct (Pour Test Rapide)

1. Créer un nouveau Pipeline job
2. Dans **Pipeline Definition**, sélectionner **Pipeline script**
3. **Copier-coller le code suivant dans le champ "Script"** :

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK17'
    }
    
    environment {
        JAVA_HOME = "${tool 'JDK17'}"
        MAVEN_HOME = "${tool 'Maven'}"
        PATH = "${env.JAVA_HOME}/bin:${env.MAVEN_HOME}/bin:${env.PATH}"
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
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline réussi avec succès!'
        }
        failure {
            echo 'Pipeline a échoué!'
        }
    }
}
```

4. **Important** : Si vous utilisez cette option, vous devez d'abord configurer le projet Git dans la section **General** → **Source Code Management** → **Git** avec l'URL : `https://github.com/NegzaouiOussama/Deveops.git`
5. Cliquer sur **Save**

## Étape 6 : Exécution du Pipeline

1. Dans la page du job créé, cliquer sur **Build Now**
2. Le pipeline va commencer à s'exécuter
3. Cliquer sur le numéro de build dans **Build History** pour voir les détails
4. Cliquer sur **Console Output** pour voir les logs en temps réel

## Étape 7 : Vérification des Étapes

Le pipeline exécute les étapes suivantes :

### Stage 1 : Checkout
- Récupère le code source depuis le dépôt Git

### Stage 2 : Build
- Exécute `mvn clean compile`
- Compile le code source Java

### Stage 3 : Test
- Exécute `mvn test`
- Lance les tests unitaires
- Génère les rapports JUnit dans `target/surefire-reports/*.xml`

### Stage 4 : Package
- Exécute `mvn clean package -DskipTests`
- Crée le fichier JAR dans `target/`
- Archive l'artefact JAR

## Étape 8 : Consultation des Résultats

### Voir les Logs :
- Cliquer sur le build → **Console Output**
- Voir tous les logs d'exécution

### Voir les Rapports de Tests :
- Cliquer sur le build → **Test Result**
- Voir les résultats des tests JUnit

### Télécharger les Artefacts :
- Cliquer sur le build → **Artifacts**
- Télécharger le fichier `student-management-0.0.1-SNAPSHOT.jar`

## Dépannage

### Problème : "Maven not found"
**Solution** : Vérifier que Maven est bien configuré dans **Global Tool Configuration** avec le bon chemin.

### Problème : "JDK not found"
**Solution** : Vérifier que JDK 17 est bien configuré dans **Global Tool Configuration** avec le bon chemin.

### Problème : "Cannot connect to Git repository"
**Solution** : 
- Vérifier les credentials Git
- Si le projet est local, utiliser un chemin de fichier ou configurer un serveur Git local
- Vérifier la connectivité réseau

### Problème : "Tests failing"
**Solution** :
- Vérifier que tous les tests passent localement avec `mvn test`
- Vérifier la configuration de la base de données dans `application.properties`
- Vérifier que MySQL est démarré (si nécessaire)

### Problème : "Build failing at compile"
**Solution** :
- Vérifier que le projet compile localement avec `mvn clean compile`
- Vérifier les versions de Java et Maven
- Vérifier les dépendances dans `pom.xml`

## Commandes Utiles pour Test Local

Avant de tester dans Jenkins, vous pouvez tester localement :

```bash
# Compiler le projet
mvn clean compile

# Exécuter les tests
mvn test

# Créer le package
mvn clean package

# Vérifier la version de Maven
mvn -version

# Vérifier la version de Java
java -version
```

## Structure du Jenkinsfile

Le Jenkinsfile contient :
- **Agent** : `any` (utilise n'importe quel agent disponible)
- **Tools** : Configuration de Maven et JDK 17
- **Environment** : Variables d'environnement (JAVA_HOME, MAVEN_HOME, PATH)
- **Stages** : Checkout, Build, Test, Package
- **Post Actions** : Nettoyage, messages de succès/échec

## Prochaines Étapes (Optionnel)

Pour améliorer le pipeline, vous pouvez ajouter :
- **Stage Deploy** : Déploiement automatique
- **Stage SonarQube** : Analyse de code
- **Notifications** : Email/Slack en cas d'échec
- **Docker** : Build d'images Docker
- **Kubernetes** : Déploiement sur K8s

## Support

Pour plus d'informations :
- Documentation Jenkins : https://www.jenkins.io/doc/
- Documentation Pipeline : https://www.jenkins.io/doc/book/pipeline/

