# CurrencyConverter

Application Android Kotlin de conversion de devises avec taux fixes.

## Fonctionnalites

- Interface ConstraintLayout et clavier TableLayout 4 x 3.
- Deux Spinner alimentes depuis strings.xml : USD, EUR, TND, GBP, JPY, CAD.
- Saisie via le clavier numerique de l application uniquement.
- Conversion avec l euro comme devise pivot, resultat arrondi a 2 decimales.
- Bouton d inversion des devises avec relance de la conversion.
- Regles : un seul point decimal, 10 caracteres maximum, touche C pour reinitialiser.
- Toast si le montant vaut 0.
- Bonus : conversion automatique a chaque frappe ou changement de devise.

## Build

Ouvrir le projet dans Android Studio ou executer:

    ./gradlew assembleDebug

Le projet utilise AGP 9.4.1 avec Kotlin integre et Gradle 9.6.0.

## Captures

Les captures d ecran se trouvent dans le dossier screenshots/.
## Structure du projet

- `app/src/main/java/com/example/currencyconverter/MainActivity.kt` : logique complete.
- `app/src/main/res/layout/activity_main.xml` : ConstraintLayout et clavier TableLayout.
- `app/src/main/res/values/` : strings, colors, dimens et theme.
- `screenshots/` : captures de l ecran initial et des quatre cas de test.
- `docs/rapport.pdf` : compte rendu au format PDF.
- `scripts/capture_screenshots.ps1` : script de reproduction des captures sur emulateur.
## Resultats verifies

| Montant | Source | Cible | Resultat |
|---:|---|---|---:|
| 120 | USD | EUR | 102.56 |
| 100 | EUR | TND | 340.00 |
| 50 | GBP | USD | 67.24 |
| 10 | EUR | EUR | 10.00 |

## Notes techniques

Le projet utilise Android Gradle Plugin 9.4.1, Gradle 9.6.0 et le Kotlin integre a AGP. Le plugin org.jetbrains.kotlin.android n est pas necessaire (et provoquerait un conflit avec AGP 9).

Pour regenerer les captures avec un emulateur demarre :

    powershell -ExecutionPolicy Bypass -File scripts\capture_screenshots.ps1
