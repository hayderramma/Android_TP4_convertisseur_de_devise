# Script utilitaire :Capture les ecrans de test sur l'emulateur Android.
# Usage : powershell -ExecutionPolicy Bypass -File scripts\capture_screenshots.ps1

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$projet = Split-Path -Parent $PSScriptRoot
$dossierCaptures = Join-Path $projet "screenshots"
$fichierResultats = Join-Path $dossierCaptures "resultats.txt"
New-Item -ItemType Directory -Force $dossierCaptures | Out-Null

function Tap($x, $y) {
    & $adb shell input tap $x $y | Out-Null
    Start-Sleep -Milliseconds 350
}

function Obtenir-Dump {
    $cheminLocal = "$env:TEMP\currency_window.xml"
    for ($essai = 0; $essai -lt 5; $essai++) {
        Remove-Item $cheminLocal -ErrorAction SilentlyContinue
        & $adb shell rm /sdcard/window.xml 2>$null | Out-Null
        & $adb shell uiautomator dump /sdcard/window.xml 2>$null | Out-Null
        & $adb pull /sdcard/window.xml $cheminLocal 2>$null | Out-Null
        if (Test-Path $cheminLocal) {
            return [xml](Get-Content $cheminLocal -Raw)
        }
        Start-Sleep -Milliseconds 300
    }
    throw "Impossible de recuperer le dump d'interface."
}

function Lire-Valeurs {
    $xml = Obtenir-Dump
    $noeuds = Select-Xml -XPath '//node' -Xml $xml | ForEach-Object { $_.Node }
    $montant = ($noeuds | Where-Object { $_.'resource-id' -eq 'com.example.currencyconverter:id/tvMontant' }).text
    $resultat = ($noeuds | Where-Object { $_.'resource-id' -eq 'com.example.currencyconverter:id/tvResultat' }).text
    return "Montant=$montant Resultat=$resultat"
}

function Capturer($nom) {
    & $adb shell screencap -p /sdcard/currency_shot.png | Out-Null
    & $adb pull /sdcard/currency_shot.png (Join-Path $dossierCaptures $nom) | Out-Null
}

function Choisir-Devise($spinnerX, $spinnerY, $devise) {
    Tap $spinnerX $spinnerY

    $item = $null
    for ($tentative = 0; $tentative -lt 6 -and $null -eq $item; $tentative++) {
        Start-Sleep -Milliseconds 350
        $xml = Obtenir-Dump
        $noeuds = Select-Xml -XPath '//node' -Xml $xml | ForEach-Object { $_.Node }
        $item = $noeuds | Where-Object {
            $_.text -eq $devise -and $_.'class' -eq 'android.widget.CheckedTextView'
        } | Select-Object -First 1
        if ($null -eq $item -and $tentative -eq 2) {
            # La popup a pu etre fermee par une animation : on reouvre.
            Tap $spinnerX $spinnerY
        }
    }

    if ($null -eq $item) { throw "Devise introuvable dans le dropdown : $devise" }

    $m = [regex]::Match($item.bounds, '\[(\d+),(\d+)\]\[(\d+),(\d+)\]')
    $x = [int](([int]$m.Groups[1].Value + [int]$m.Groups[3].Value) / 2)
    $y = [int](([int]$m.Groups[2].Value + [int]$m.Groups[4].Value) / 2)
    Tap $x $y
    # Laisse le temps a la popup de se fermer avant la prochaine interaction.
    Start-Sleep -Milliseconds 350
}

function Saisir($texte) {
    $coordonnees = @{
        '0' = @(540, 2235); '1' = @(180, 1905); '2' = @(540, 1905); '3' = @(900, 1905)
        '4' = @(180, 1575); '5' = @(540, 1575); '6' = @(900, 1575)
        '7' = @(180, 1245); '8' = @(540, 1245); '9' = @(900, 1245)
    }
    foreach ($chiffre in $texte.ToCharArray()) {
        $point = $coordonnees[[string]$chiffre]
        Tap $point[0] $point[1]
    }
}

function Reinitialiser {
    Tap 180 2235
}

$rapport = @()

# Ecran au demarrage.
& $adb shell am force-stop com.example.currencyconverter | Out-Null
& $adb shell am start -n com.example.currencyconverter/.MainActivity | Out-Null
Start-Sleep -Milliseconds 900
Capturer "01_demarrage.png"
$rapport += "01_demarrage.png : " + (Lire-Valeurs)

# Cas 1 : 120 USD -> EUR.
Choisir-Devise 210 215 'USD'
Choisir-Devise 210 482 'EUR'
Reinitialiser
Saisir '120'
Tap 540 822
Capturer "02_120_USD_EUR.png"
$rapport += "02_120_USD_EUR.png : " + (Lire-Valeurs)

# Cas 2 : 100 EUR -> TND.
Choisir-Devise 210 215 'EUR'
Choisir-Devise 210 482 'TND'
Reinitialiser
Saisir '100'
Tap 540 822
Capturer "03_100_EUR_TND.png"
$rapport += "03_100_EUR_TND.png : " + (Lire-Valeurs)

# Cas 3 : 50 GBP -> USD.
Choisir-Devise 210 215 'GBP'
Choisir-Devise 210 482 'USD'
Reinitialiser
Saisir '50'
Tap 540 822
Capturer "04_50_GBP_USD.png"
$rapport += "04_50_GBP_USD.png : " + (Lire-Valeurs)

# Cas 4 : 10 EUR -> EUR.
Choisir-Devise 210 215 'EUR'
Choisir-Devise 210 482 'EUR'
Reinitialiser
Saisir '10'
Tap 540 822
Capturer "05_10_EUR_EUR.png"
$rapport += "05_10_EUR_EUR.png : " + (Lire-Valeurs)

$rapport | Set-Content -Path $fichierResultats -Encoding UTF8
$rapport
