// Fichier Gradle racine : déclaration des plugins utilisés par les modules.
// À partir d'AGP 9, la compilation Kotlin est intégrée au plugin Android :
// aucun plugin org.jetbrains.kotlin.android n'est nécessaire.
plugins {
    id("com.android.application") version "9.4.1" apply false
}
