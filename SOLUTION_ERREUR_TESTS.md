# Solution à l'Erreur des Tests dans Jenkins

## Problème

Les tests échouent dans Jenkins avec l'erreur :
```
Connection refused
Unable to determine Dialect without JDBC metadata
```

**Cause** : MySQL n'est pas accessible dans l'environnement Jenkins lors de l'exécution des tests.

## Solution Implémentée

### 1. Ajout de la dépendance H2 pour les tests

Dans `pom.xml`, ajout de la dépendance H2 (base de données en mémoire) :

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. Création du profil de test

Création du fichier `src/test/resources/application-test.properties` :

```properties
# Configuration pour les tests - utilise H2 en mémoire au lieu de MySQL
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Désactiver la console H2 pour les tests
spring.h2.console.enabled=false
```

### 3. Activation du profil test dans les tests

Modification de `StudentManagementApplicationTests.java` :

```java
@SpringBootTest
@ActiveProfiles("test")
class StudentManagementApplicationTests {
    // ...
}
```

## Avantages de cette Solution

✅ **Tests indépendants** : Les tests n'ont plus besoin de MySQL
✅ **Exécution rapide** : H2 en mémoire est plus rapide que MySQL
✅ **Pas de configuration externe** : Pas besoin de configurer MySQL dans Jenkins
✅ **Isolation** : Chaque test utilise sa propre base de données en mémoire

## Comment ça fonctionne

1. Lors de l'exécution des tests, Spring Boot détecte le profil `test`
2. Il charge `application-test.properties` au lieu de `application.properties`
3. H2 est utilisé en mémoire au lieu de MySQL
4. Les tests s'exécutent sans avoir besoin de MySQL

## Vérification

Après ces modifications, les tests devraient passer dans Jenkins :

```bash
mvn test
```

## Alternative : Ignorer les tests (Non recommandé)

Si vous voulez temporairement ignorer les tests dans Jenkins, vous pouvez modifier le Jenkinsfile :

```groovy
stage('Test') {
    steps {
        sh 'mvn test -DskipTests'  // Ignore les tests
    }
}
```

**⚠️ Attention** : Cette solution n'est pas recommandée car elle ne vérifie pas que le code fonctionne correctement.

## Résumé

- ✅ Ajout de H2 comme dépendance de test
- ✅ Création de `application-test.properties` avec configuration H2
- ✅ Activation du profil `test` dans les tests
- ✅ Les tests peuvent maintenant s'exécuter sans MySQL

Les tests devraient maintenant passer dans Jenkins ! 🎉

