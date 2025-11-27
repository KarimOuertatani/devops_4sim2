# Pourquoi les liens du Workspace ne sont pas cliquables dans Jenkins ?

## Problème

Les liens dans la page "Workspaces" de Jenkins ne sont pas cliquables car le workspace a été supprimé après l'exécution du pipeline.

## Cause

Dans le Jenkinsfile, la commande `cleanWs()` était dans la section `post { always { ... } }`, ce qui signifie qu'elle s'exécute **toujours** après chaque build, même en cas de succès.

```groovy
post {
    always {
        cleanWs()  // ❌ Supprime le workspace après chaque build
    }
}
```

## Solution

### Option 1 : Supprimer complètement `cleanWs()` (Recommandé pour le développement)

Le workspace sera conservé et vous pourrez explorer les fichiers :

```groovy
post {
    success {
        echo 'Pipeline réussi avec succès!'
    }
    failure {
        echo 'Pipeline a échoué!'
    }
    // cleanWs() supprimé - le workspace est conservé
}
```

**Avantages** :
- ✅ Les liens sont cliquables
- ✅ Vous pouvez explorer les fichiers générés
- ✅ Utile pour le débogage

**Inconvénients** :
- ⚠️ Les workspaces s'accumulent et prennent de l'espace disque

### Option 2 : Nettoyer seulement en cas d'échec

Nettoyer le workspace seulement si le build échoue :

```groovy
post {
    success {
        echo 'Pipeline réussi avec succès!'
    }
    failure {
        echo 'Pipeline a échoué!'
        cleanWs()  // Nettoie seulement en cas d'échec
    }
}
```

### Option 3 : Nettoyer seulement les builds anciens

Utiliser une stratégie de nettoyage conditionnelle :

```groovy
post {
    success {
        echo 'Pipeline réussi avec succès!'
    }
    failure {
        echo 'Pipeline a échoué!'
    }
    cleanup {
        // Nettoie seulement si le build est plus ancien que 7 jours
        script {
            def buildAge = currentBuild.getDuration()
            if (buildAge > 7 * 24 * 60 * 60 * 1000) {
                cleanWs()
            }
        }
    }
}
```

## Modification appliquée

J'ai supprimé `cleanWs()` du Jenkinsfile. Maintenant :

1. ✅ Le workspace est **conservé** après chaque build
2. ✅ Les liens dans "Workspaces" sont **cliquables**
3. ✅ Vous pouvez explorer les fichiers générés (JAR, rapports, etc.)

## Comment accéder aux fichiers

1. Aller dans **JobPipeline** → **Build #X** → **Workspaces**
2. Cliquer sur le lien du workspace (ex: `/var/lib/jenkins/jobs/JobPipeline/workspace`)
3. Naviguer dans les dossiers :
   - `target/` : contient le JAR généré
   - `target/surefire-reports/` : contient les rapports de tests
   - `src/` : contient le code source

## Nettoyage manuel (si nécessaire)

Si vous voulez nettoyer les anciens workspaces manuellement :

1. Aller dans **Manage Jenkins** → **System**
2. Ou utiliser la commande dans un terminal Jenkins :
   ```bash
   rm -rf /var/lib/jenkins/jobs/JobPipeline/workspace/*
   ```

## Recommandation

Pour un environnement de **développement/test** :
- ✅ **Conserver** le workspace (pas de `cleanWs()`)
- Permet de déboguer et explorer les fichiers

Pour un environnement de **production** :
- ⚠️ **Nettoyer** le workspace après chaque build
- Économise l'espace disque
- Utiliser `cleanWs()` dans `cleanup` ou `always`

