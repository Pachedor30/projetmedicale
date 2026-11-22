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

## Dépôt GitHub

Avant la publication, créez un dépôt GitHub vide et configurez son URL comme remote `origin`. Vérifiez ensuite que le dépôt ne contient aucun mot de passe, fichier `.env`, ni contenu généré de `target/`.
