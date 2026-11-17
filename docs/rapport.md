# Compte rendu - CurrencyConverter

## 1. Presentation

L'application CurrencyConverter convertit un montant saisi d'une devise source vers une devise cible a partir de taux fixes. Elle a ete developpee en Kotlin avec Android Studio, avec un SDK minimum API 24 et le View Binding active.

Le projet est compose d'une seule Activity (MainActivity) et d'un seul layout (activity_main.xml).
## 2. Choix d'interface

La zone d'affichage occupe environ 45 % de la hauteur. Un Guideline horizontal place a 45 % separe cette zone du clavier. Un View bleu turquoise (#2BB8E8) sert de fond a la zone haute, tandis que le clavier utilise un fond clair.

Le titre, le montant et le resultat sont des TextView. Le montant et le resultat sont alignes a droite, en 48sp, et soulignes grace a un drawable layer-list place en background. Les deux Spinner sont alimentes par un ArrayAdapter construit depuis le string-array devises. Le bouton d'inversion est un ImageButton utilisant un vector drawable.

Le clavier est un TableLayout de 4 TableRow et 3 colonnes. Chaque touche est un Button sans bordure, en 32sp. Les chiffres sont bleu #1BA6C9 et la touche C est rouge #F0727A.

Tous les textes sont dans strings.xml, toutes les couleurs dans colors.xml et les dimensions dans dimens.xml. L'ecran est bloque en portrait dans le manifeste.
## 3. Logique de conversion

Les taux sont stockes dans une Map Kotlin declaree dans MainActivity. Ils representent la valeur d'une devise pour 1 EUR.

La conversion passe par l'euro comme devise pivot : montantEnEur = montant / tauxSource, puis resultat = montantEnEur * tauxCible.

Le resultat est formate avec String.format(Locale.US, format a deux decimales, resultat) pour garantir un point decimal, puis affiche dans tvResultat. Quand source et cible sont identiques, le calcul redonne le montant initial.

## 4. Regles de gestion

- Le montant initial vaut 0 et le premier chiffre tape le remplace.
- Un seul point decimal est accepte ; une deuxieme pression sur le point est ignoree.
- La saisie est limitee a 10 caracteres.
- La touche C remet le montant a 0 et vide le resultat.
- Si le montant vaut 0, le clic sur Convertir affiche un Toast.
- Le bouton d'inversion echange les deux Spinner et relance la conversion si un resultat existe.
## 5. Tests effectues

| Montant | Source | Cible | Resultat obtenu |
|---:|---|---|---:|
| 120 | USD | EUR | 102.56 |
| 100 | EUR | TND | 340.00 |
| 50 | GBP | USD | 67.24 |
| 10 | EUR | EUR | 10.00 |

Les captures sont dans le dossier screenshots/.
## 6. Difficulites rencontrees

La principale difficulite etait liee a la version d'Android Gradle Plugin. AGP 9 integre desormais la compilation Kotlin : l'ancien plugin org.jetbrains.kotlin.android provoquait une erreur de conflit d'extension. Il a donc ete retire du projet.

Une autre difficulite etait l'alignement du montant et du resultat avec les Spinner dans la zone turquoise. L'utilisation des contraintes de baseline et d'un drawable de soulignement a permis d'obtenir un rendu stable.

## 7. Conclusion

L'application respecte l'ensemble des objectifs du TP. La conversion est correcte pour les quatre cas de test, le clavier personnalise fonctionne sans clavier systeme et le bouton d'inversion recalcule le resultat. Le bonus de conversion automatique est egalement implemente.
