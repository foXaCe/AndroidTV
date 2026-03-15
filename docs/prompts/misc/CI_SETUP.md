# CI Setup — VegafoX for Android TV

> Date : 2026-03-08

## 1. Secrets GitHub Actions

Configurer dans : **GitHub → Settings → Secrets and variables → Actions**

| Secret | Valeur | Commande |
|--------|--------|----------|
| `KEYSTORE_BASE64` | Keystore encodé en base64 | `base64 -w 0 vegafox-release.jks` |
| `KEYSTORE_PASSWORD` | Mot de passe du keystore | Dans `keystore.properties` → `storePassword` |
| `KEY_ALIAS` | Alias de la clé | Dans `keystore.properties` → `keyAlias` |
| `KEY_PASSWORD` | Mot de passe de la clé | Dans `keystore.properties` → `keyPassword` |

### Encoder le keystore

```sh
# Linux/Mac — copier dans le presse-papier
base64 -w 0 vegafox-release.jks | xclip -selection clipboard

# Linux/Mac — écrire dans un fichier
base64 -w 0 vegafox-release.jks > keystore_base64.txt

# Windows
certutil -encode vegafox-release.jks tmp.b64 && findstr /v /c:- tmp.b64 > encoded.txt
```

## 2. Workflow CI

Fichier : `.github/workflows/ci.yml`

### Étapes exécutées

| Étape | Condition | Description |
|-------|-----------|-------------|
| Ktlint check | Toujours | Vérification formatage Kotlin |
| Detekt | Toujours | Analyse statique Kotlin |
| Android Lint | Toujours | Lint Android (AGP) |
| Build Release | Push main/develop + PRs internes | Build signé via secrets |
| Build Debug | PRs externes (forks) | Build non signé, pas de secrets |
| Upload reports | En cas d'échec | Artifacts lint/detekt |

### Fonctionnement du signing

Le `signingConfigs` dans `app/build.gradle.kts` supporte deux modes :

1. **CI** : si `KEYSTORE_BASE64` est défini en env var
   - Décode le keystore depuis la variable d'environnement
   - Lit les mots de passe depuis `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
   - Le keystore décodé est écrit dans `build/ci-release.keystore` (gitignored)

2. **Local** : si `keystore.properties` existe à la racine du projet
   - Lit le chemin du keystore et les mots de passe depuis le fichier
   - Format du fichier :
     ```properties
     storeFile=vegafox-release.jks
     storePassword=***
     keyAlias=vegafox
     keyPassword=***
     ```

### Sécurité

- Les secrets GitHub ne sont **jamais** exposés dans les logs CI
- Les PRs de forks n'ont **pas accès** aux secrets → build debug uniquement
- `.gitignore` protège : `*.keystore`, `*.jks`, `keystore.properties`

## 3. Dependabot

Fichier : `.github/dependabot.yml`

- Vérifie les dépendances Gradle chaque lundi à 09h00 (Europe/Paris)
- Groupe les mises à jour par écosystème (Compose, AndroidX, Kotlin, Jellyfin)
- Limite à 5 PRs simultanées pour les dépendances, 3 pour les Actions
- Vérifie aussi les versions des GitHub Actions

## 4. Dépendances mises à jour (2026-03-08)

| Bibliothèque | Avant | Après | Raison |
|-------------|-------|-------|--------|
| Ktor | 3.0.3 | 3.4.0 | CVE Netty (10 alertes : HTTP/2 DDoS, CRLF injection, DoS) |
| Coil | 3.3.0 | 3.4.0 | Dernière version stable |

### Alertes Dependabot restantes (transitives non contrôlables)

| Dépendance transitive | Alertes | Source probable | Action |
|-----------------------|---------|-----------------|--------|
| logback-core | 5 (low→high) | Jellyfin SDK | Attendre mise à jour SDK |
| jose4j | 1 (high) | Jellyfin SDK | Attendre mise à jour SDK |
| jdom2 | 1 (high) | NewPipe Extractor | Attendre mise à jour NPE |
| protobuf | 2 (high) | AndroidX/gRPC | Attendre mise à jour AndroidX |
| commons-lang3 | 1 (medium) | Transitive | Attendre mise à jour parent |
| httpclient | 1 (medium) | Transitive | Attendre mise à jour parent |

Ces dépendances transitives seront résolues automatiquement quand leurs parents directs (Jellyfin SDK, NewPipe Extractor, AndroidX) publieront des mises à jour. Dependabot créera automatiquement des PRs.

## 5. Fichiers de configuration

| Fichier | Rôle |
|---------|------|
| `.github/workflows/ci.yml` | Pipeline CI |
| `.github/dependabot.yml` | Mises à jour automatiques dépendances |
| `app/build.gradle.kts` | Signing config (CI + local) |
| `keystore.properties` | Config keystore locale (gitignored) |
| `SECURITY.md` | Politique de signalement vulnérabilités |
