package com.example.currencyconverter

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.currencyconverter.databinding.ActivityMainBinding
import java.util.Locale

/**
 * Écran unique du convertisseur : saisie via un clavier personnalisé,
 * choix des devises avec deux Spinner et affichage du résultat converti.
 */
class MainActivity : Activity() {

    // View Binding : plus besoin de findViewById.
    private lateinit var binding: ActivityMainBinding

    // Taux fixes fournis par l'énoncé, exprimés pour 1 EUR.
    private val taux = mapOf(
        "EUR" to 1.0,
        "USD" to 1.17,
        "TND" to 3.40,
        "GBP" to 0.87,
        "JPY" to 172.50,
        "CAD" to 1.62
    )

    // Évite de déclencher une conversion pendant l'initialisation des Spinner.
    private var conversionAutoActivee = false

    // Permet de savoir si le bouton d'inversion doit relancer la conversion.
    private var resultatDejaAffiche = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialiserLesDevises()
        installerLeClavierNumerique()

        binding.btnConvertir.setOnClickListener {
            convertirEtAfficher(afficherErreurSaisie = true)
        }
        binding.btnInverser.setOnClickListener {
            inverserLesDevises()
        }
    }

    /** Remplit les deux Spinner depuis le string-array R.array.devises. */
    private fun initialiserLesDevises() {
        val devises = resources.getStringArray(R.array.devises)

        binding.spDeviseSource.adapter = creerAdaptateur(devises)
        binding.spDeviseCible.adapter = creerAdaptateur(devises)

        // Sélections par défaut demandées : USD vers EUR.
        binding.spDeviseSource.setSelection(devises.indexOf(DEVISE_SOURCE_DEFAUT))
        binding.spDeviseCible.setSelection(devises.indexOf(DEVISE_CIBLE_DEFAUT))

        // Bonus : conversion automatique dès qu'une devise change.
        val ecouteurDevise = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (conversionAutoActivee) {
                    convertirEtAfficher(afficherErreurSaisie = false)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.spDeviseSource.onItemSelectedListener = ecouteurDevise
        binding.spDeviseCible.onItemSelectedListener = ecouteurDevise
        conversionAutoActivee = true
    }

    /** Construit un ArrayAdapter partageant la même présentation pour les deux listes. */
    private fun creerAdaptateur(devises: Array<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_spinner_item, devises).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    /** Associe chaque touche du TableLayout à son action. */
    private fun installerLeClavierNumerique() {
        val touchesChiffres = arrayOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9",
            binding.btnPoint to POINT_DECIMAL
        )

        touchesChiffres.forEach { (bouton, caractere) ->
            bouton.setOnClickListener { ajouterCaractere(caractere) }
        }

        binding.btnC.setOnClickListener { reinitialiserLaSaisie() }
    }

    /**
     * Ajoute un caractère à la fin du montant.
     * Le premier chiffre remplace le "0" initial, un seul point est autorisé
     * et la saisie est limitée à 10 caractères.
     */
    private fun ajouterCaractere(c: String) {
        val saisieActuelle = binding.tvMontant.text.toString()
        if (saisieActuelle.length >= LONGUEUR_MAXIMALE_MONTANT) return

        val nouvelleSaisie = when {
            saisieActuelle == MONTANT_INITIAL && c == POINT_DECIMAL ->
                MONTANT_INITIAL + POINT_DECIMAL

            saisieActuelle == MONTANT_INITIAL -> c

            c == POINT_DECIMAL && saisieActuelle.contains(POINT_DECIMAL) -> return

            else -> saisieActuelle + c
        }

        binding.tvMontant.text = nouvelleSaisie

        // Bonus : recalcule le résultat à chaque frappe clavier.
        convertirEtAfficher(afficherErreurSaisie = false)
    }

    /** Remet le montant à zéro et efface le résultat. */
    private fun reinitialiserLaSaisie() {
        binding.tvMontant.text = MONTANT_INITIAL
        binding.tvResultat.text = getString(R.string.resultat_initial)
        resultatDejaAffiche = false
    }

    /** Échange les deux devises et relance la conversion si un résultat était affiché. */
    private fun inverserLesDevises() {
        val positionSource = binding.spDeviseSource.selectedItemPosition
        val positionCible = binding.spDeviseCible.selectedItemPosition

        binding.spDeviseSource.setSelection(positionCible)
        binding.spDeviseCible.setSelection(positionSource)

        if (resultatDejaAffiche) {
            convertirEtAfficher(afficherErreurSaisie = false)
        }
    }

    /**
     * Lit la saisie, vérifie qu'un montant non nul a été entré, convertit
     * puis affiche le résultat arrondi à deux décimales.
     */
    private fun convertirEtAfficher(afficherErreurSaisie: Boolean) {
        val montant = lireMontantSaisi()
        if (montant == null || montant == 0.0) {
            if (afficherErreurSaisie) {
                Toast.makeText(
                    this,
                    R.string.toast_montant_vide,
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.tvResultat.text = getString(R.string.resultat_initial)
            resultatDejaAffiche = false
            return
        }

        val source = binding.spDeviseSource.selectedItem.toString()
        val cible = binding.spDeviseCible.selectedItem.toString()
        val resultat = convertir(montant, source, cible)

        // Locale.US garantit le séparateur décimal '.' attendu dans les tests.
        binding.tvResultat.text = String.format(
            Locale.US,
            getString(R.string.format_resultat),
            resultat
        )
        resultatDejaAffiche = true
    }

    /** Convertit une chaîne éventuellement incomplète ("12.") en Double. */
    private fun lireMontantSaisi(): Double? {
        val texte = binding.tvMontant.text.toString().trim()
        if (texte.isEmpty()) return null
        val texteNormalise = if (texte.endsWith(POINT_DECIMAL)) texte + "0" else texte
        return texteNormalise.toDoubleOrNull()
    }

    /**
     * Conversion avec l'euro comme devise pivot :
     * montant en EUR = montant / taux(source), puis résultat = EUR * taux(cible).
     */
    private fun convertir(montant: Double, source: String, cible: String): Double {
        val tauxSource = requireNotNull(taux[source]) { "Devise source inconnue : $source" }
        val tauxCible = requireNotNull(taux[cible]) { "Devise cible inconnue : $cible" }
        val montantEnEuro = montant / tauxSource
        return montantEnEuro * tauxCible
    }

    private companion object {
        const val MONTANT_INITIAL = "0"
        const val POINT_DECIMAL = "."
        const val LONGUEUR_MAXIMALE_MONTANT = 10
        const val DEVISE_SOURCE_DEFAUT = "USD"
        const val DEVISE_CIBLE_DEFAUT = "EUR"
    }
}
