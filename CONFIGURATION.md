# Configuration PostgreSQL et publication

## Connexion PostgreSQL sous WildFly

L'application obtient ses connexions par la datasource JNDI `CarnetsDS`. Les DAO et `Bdd` ne contiennent donc pas d'adresse de serveur ni d'identifiants PostgreSQL.

La configuration WildFly est définie dans `wildfly-jaas/configure-carnets-datasource.cli` et lit les variables d'environnement suivantes :

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASS`

Les cinq variables sont obligatoires pour la datasource ; le script n'inscrit pas d'hôte ou de mot de passe de secours.

Définissez ces variables dans l'environnement du processus WildFly **avant son démarrage**, puis appliquez le script avec la CLI de WildFly :

```powershell
& 'CHEMIN_VERS_WILDFLY\bin\jboss-cli.bat' --connect --file='wildfly-jaas\configure-carnets-datasource.cli'
```

Sous Windows, pour un lancement manuel dans PowerShell, définissez-les dans la même session avant de démarrer WildFly :

```powershell
$env:DB_HOST = 'localhost'
$env:DB_PORT = '5432'
$env:DB_NAME = 'gestion_carnets_medicaux'
$env:DB_USER = 'carnets_app'
$env:DB_PASS = 'votre-secret-local'
```

`.env.example` documente les noms attendus ; WildFly ne charge pas automatiquement un fichier `.env`. Ne publiez jamais le vrai mot de passe. Les fichiers `.env` locaux sont ignorés par Git.

Le fichier Maven `pom.xml`, qui déclare les dépendances de l'application WAR, se trouve déjà à la racine du projet.

## Déploiement Render avec Docker

À la création du service Web Render, sélectionnez le runtime **Docker** et gardez la racine du dépôt comme répertoire du projet. Le `Dockerfile` à la racine construit le WAR avec Java 21, installe le pilote PostgreSQL et le module de sécurité JAAS dans WildFly 41, puis déploie l'application.

Ajoutez au service Web les variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` et `DB_PASS` avec les valeurs de la base PostgreSQL Render. Utilisez l'hôte interne de la base et placez les deux services dans la même région. Render définit `PORT` pour le serveur Web ; WildFly s'y lie sur `0.0.0.0`. L'application est publiée à la racine du domaine Render.

Le fichier `docker/standalone.xml` est la configuration WildFly dédiée au conteneur. Il référence ces variables à l'exécution ; le script CLI destiné au serveur WildFly local n'est pas nécessaire pour le déploiement Docker.

## Dépôt GitHub

Avant la publication, créez un dépôt GitHub vide et configurez son URL comme remote `origin`. Vérifiez ensuite que le dépôt ne contient aucun mot de passe, fichier `.env`, ni contenu généré de `target/`.
