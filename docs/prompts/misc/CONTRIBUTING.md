# Contributing — VegafoX for Android TV

Repository: https://github.com/foXaCe/AndroidTV

## Quality tools

| Tool | Version | Purpose |
|------|---------|---------|
| **Ktlint** | 12.1.2 (jlleitschuh/ktlint-gradle) | Kotlin code formatting |
| **Detekt** | 1.23.8 | Kotlin static analysis |
| **Android Lint** | AGP 9.0.0 built-in | Android-specific checks |

All three tools use a **baseline approach**: existing issues in legacy code are accepted,
but any new violation will fail the build.

- Detekt: `maxIssues: 314` in `detekt.yaml` — reduce over time
- Android Lint: `app/lint-baseline.xml` — auto-generated baseline

## Commands

```sh
# Formatting
./gradlew ktlintCheck          # Check formatting (no changes)
./gradlew ktlintFormat         # Auto-fix formatting

# Static analysis
./gradlew detekt               # Report in build/reports/detekt/

# Android Lint
./gradlew :app:lint            # Report in app/build/reports/lint/

# All checks at once
./gradlew checkAll             # Ktlint + Detekt + Android Lint
```

## Workflow before each commit/push

```sh
1. ./gradlew ktlintFormat      # Auto-fix formatting
2. ./gradlew checkAll           # Verify everything passes
3. git add <files>
4. git commit -m "type: description"
5. git push origin main
```

## Commit message convention (Conventional Commits)

```
feat: nouvelle fonctionnalite
fix: correction de bug
refactor: refactoring sans changement de comportement
style: formatage, pas de changement logique
perf: amelioration de performance
chore: maintenance, dependances
docs: documentation
test: ajout ou correction de tests
```

## Pre-commit hook

Install the Git pre-commit hook to run Ktlint + Detekt automatically before each commit:

```sh
sh scripts/setup-hooks.sh
```

The hook runs `ktlintCheck` and `detekt`. If either fails, the commit is blocked.
Run `./gradlew ktlintFormat` to auto-fix formatting issues, then commit again.

## CI (GitHub Actions)

Every push to `main`/`develop` and every PR to `main` triggers:

1. Ktlint check
2. Detekt
3. Android Lint
4. Release build (signed, internal PRs and pushes only)
5. Debug build (external PRs from forks)

Reports are uploaded as artifacts if any step fails.

## CI Secrets (GitHub → Settings → Secrets and variables → Actions)

| Secret | Comment l'obtenir |
|--------|------------------|
| `KEYSTORE_BASE64` | `base64 -w 0 release.keystore` |
| `KEYSTORE_PASSWORD` | Mot de passe du keystore |
| `KEY_ALIAS` | Alias de la clé dans le keystore |
| `KEY_PASSWORD` | Mot de passe de la clé |

Pour encoder le keystore :

```
Linux/Mac : base64 -w 0 release.keystore | xclip
Windows   : certutil -encode release.keystore tmp.b64 && findstr /v /c:- tmp.b64 > encoded.txt
```

## Configuration files

| File | Purpose |
|------|---------|
| `.editorconfig` | Editor settings (tabs, max line length 140) |
| `detekt.yaml` | Detekt rules (baseline: 314 issues) |
| `android-lint.xml` | Android Lint rules |
| `app/lint-baseline.xml` | Android Lint baseline (191 errors, 1078 warnings filtered) |
| `scripts/pre-commit` | Git pre-commit hook (versioned) |
| `scripts/setup-hooks.sh` | Hook installer |
| `.github/workflows/ci.yml` | GitHub Actions CI pipeline |
