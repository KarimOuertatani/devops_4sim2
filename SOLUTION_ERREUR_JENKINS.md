# Solution à l'Erreur Jenkins : "Tool type does not have an install configured"

## Erreur Rencontrée

```
Tool type "maven" does not have an install of "Maven" configured - did you mean "Maven3"?
Tool type "jdk" does not have an install of "JDK17" configured
```

## Solution Rapide

### Option 1 : Utiliser Maven3 (Recommandé)

Le code corrigé à copier dans le champ **Script** :

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

## Configuration des Outils dans Jenkins

### Étape 1 : Configurer Maven3

1. Aller dans **Manage Jenkins** → **Global Tool Configuration**
2. Dans la section **Maven**, vérifier s'il y a déjà une installation nommée `Maven3`
3. Si elle n'existe pas :
   - Cliquer sur **Add Maven**
   - **Name** : `Maven3` (exactement comme ça)
   - **MAVEN_HOME** : Entrer le chemin vers Maven
     - Windows : `C:\Program Files\Apache\maven` (ou votre chemin)
     - Linux : `/usr/share/maven` ou `/opt/maven`
   - OU cocher **Install automatically** et choisir une version (ex: `3.9.6`)
4. Cliquer sur **Save**

### Étape 2 : Vérifier Java

Le pipeline utilise maintenant le JDK du système. Vérifier que Java est installé :

- Sur la machine Jenkins, ouvrir un terminal
- Exécuter : `java -version`
- Doit afficher la version Java (17 ou supérieur recommandé)

## Option 2 : Si vous voulez configurer un JDK spécifique

Si vous voulez utiliser un JDK spécifique nommé `JDK17`, voici le code :

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }
    
    environment {
        JAVA_HOME = "${tool 'JDK17'}"
        MAVEN_HOME = "${tool 'Maven3'}"
        PATH = "${env.JAVA_HOME}/bin:${env.MAVEN_HOME}/bin:${env.PATH}"
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

Et configurer JDK17 dans **Global Tool Configuration** :
1. Section **JDK**
2. Cliquer sur **Add JDK**
3. **Name** : `JDK17`
4. **JAVA_HOME** : Chemin vers JDK 17
   - Windows : `C:\Program Files\Java\jdk-17`
   - Linux : `/usr/lib/jvm/java-17-openjdk`

## Résumé des Changements

✅ **Avant** : `maven 'Maven'` → **Maintenant** : `maven 'Maven3'`
✅ **Avant** : `jdk 'JDK17'` → **Maintenant** : Supprimé (utilise le JDK système)
✅ **Avant** : Variables JAVA_HOME et MAVEN_HOME → **Maintenant** : Seulement MAVEN_HOME

## Vérification

Après avoir corrigé le code et configuré Maven3 :

1. Cliquer sur **Save** dans la configuration du job
2. Cliquer sur **Build Now**
3. L'erreur devrait disparaître
4. Le pipeline devrait s'exécuter correctement

## Dépannage Supplémentaire

### Si Maven3 n'apparaît pas dans la liste

1. Vérifier que le plugin **Maven Integration** est installé
2. Aller dans **Manage Jenkins** → **Plugins**
3. Chercher "Maven Integration" et l'installer si nécessaire
4. Redémarrer Jenkins

### Si le build échoue toujours

1. Vérifier les logs dans **Console Output**
2. Vérifier que Maven est bien installé sur la machine
3. Vérifier que Java est bien installé et accessible

