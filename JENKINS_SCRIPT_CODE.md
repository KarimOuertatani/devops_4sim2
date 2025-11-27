# Code à Copier dans le Champ Script Jenkins

## Configuration pour votre dépôt GitHub : https://github.com/NegzaouiOussama/Deveops.git

### Option 1 : Pipeline Script Direct (Copier dans le champ "Script")

Si vous utilisez **"Pipeline script"** dans la définition du pipeline, copiez ce code exactement :

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven3'
    }
    
    environment {
        MAVEN_HOME = "${tool 'Maven3'}"
        PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/NegzaouiOussama/Deveops.git'
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

### Option 2 : Pipeline depuis SCM (Recommandé)

Si vous utilisez **"Pipeline script from SCM"**, configurez :

1. **SCM** : Git
2. **Repository URL** : `https://github.com/NegzaouiOussama/Deveops.git`
3. **Branch Specifier** : `*/main`
4. **Script Path** : `Jenkinsfile`

Dans ce cas, Jenkins utilisera automatiquement le fichier `Jenkinsfile` de votre dépôt.

## Instructions Étape par Étape

### 1. Créer le Job Pipeline

1. Aller sur `http://localhost:8080`
2. Cliquer sur **New Item**
3. Nommer le job (ex: `Deveops-Pipeline`)
4. Sélectionner **Pipeline**
5. Cliquer sur **OK**

### 2. Configurer le Pipeline

#### Si vous choisissez "Pipeline script" :

1. Dans **Definition**, sélectionner **Pipeline script**
2. **Copier tout le code ci-dessus** dans le champ **Script**
3. Cliquer sur **Save**

#### Si vous choisissez "Pipeline script from SCM" :

1. Dans **Definition**, sélectionner **Pipeline script from SCM**
2. **SCM** : Sélectionner **Git**
3. **Repository URL** : `https://github.com/NegzaouiOussama/Deveops.git`
4. **Branch Specifier** : `*/main`
5. **Script Path** : `Jenkinsfile`
6. Cliquer sur **Save**

### 3. Vérifier les Outils Configurés

Avant d'exécuter, assurez-vous que dans **Manage Jenkins** → **Global Tool Configuration** :

- **Maven** est configuré avec le nom : `Maven3`
- **JDK** : Le JDK du système sera utilisé automatiquement (pas besoin de configuration si Java est installé)

### 4. Exécuter le Pipeline

1. Dans la page du job, cliquer sur **Build Now**
2. Cliquer sur le numéro de build pour voir les détails
3. Cliquer sur **Console Output** pour voir les logs

## Ce que fait le Pipeline

1. **Checkout** : Récupère le code depuis GitHub
2. **Build** : Compile le projet avec `mvn clean compile`
3. **Test** : Exécute les tests avec `mvn test` et génère les rapports JUnit
4. **Package** : Crée le fichier JAR avec `mvn clean package`

## Résultats Attendus

- ✅ **Console Output** : Affiche tous les logs d'exécution
- ✅ **Test Result** : Affiche les résultats des tests JUnit
- ✅ **Artifacts** : Contient le fichier JAR généré (`student-management-0.0.1-SNAPSHOT.jar`)

## Dépannage Rapide

### Erreur : "Maven not found" ou "Maven3 not found"
→ Aller dans **Manage Jenkins** → **Global Tool Configuration**
→ Dans la section **Maven**, vérifier qu'il y a une installation nommée `Maven3`
→ Si elle n'existe pas, cliquer sur **Add Maven** et nommer `Maven3`
→ Configurer le chemin MAVEN_HOME ou cocher "Install automatically"

### Erreur : "JDK17 not found"
→ Cette version du pipeline utilise le JDK du système
→ Vérifier que Java est installé sur la machine Jenkins avec : `java -version`
→ Si vous voulez utiliser un JDK spécifique, configurez-le dans **Global Tool Configuration** et modifiez le pipeline

### Erreur : "Cannot connect to repository"
→ Vérifier que l'URL GitHub est correcte : `https://github.com/NegzaouiOussama/Deveops.git`
→ Vérifier votre connexion internet

### Erreur : "Tests failing"
→ Vérifier que MySQL est démarré (si nécessaire)
→ Vérifier la configuration dans `application.properties`

