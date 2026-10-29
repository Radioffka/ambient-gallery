param(
    [string]$SdkRoot = $env:ANDROID_HOME,
    [string]$AvdName = 'AmbientGalleryTV',
    [string]$ApkPath = (Join-Path $PSScriptRoot '..\app\build\outputs\apk\debug\app-debug.apk')
)

$ErrorActionPreference = 'Stop'
if (-not $SdkRoot) { throw 'Pass -SdkRoot or set ANDROID_HOME to the Android SDK directory.' }
$adb = Join-Path $SdkRoot 'platform-tools\adb.exe'
$emulator = Join-Path $SdkRoot 'emulator\emulator.exe'
if (-not (Test-Path -LiteralPath $adb -PathType Leaf)) { throw "ADB not found in $SdkRoot" }
if (-not (Test-Path -LiteralPath $emulator -PathType Leaf)) { throw "Emulator not found in $SdkRoot" }
if (-not (Test-Path -LiteralPath $ApkPath -PathType Leaf)) { throw "APK not found: $ApkPath" }

$devices = & $adb devices
if ($LASTEXITCODE -ne 0) { throw 'Could not list Android devices.' }
if (-not ($devices | Select-String '^emulator-\d+\s+device$')) {
    $available = & $emulator -list-avds
    if ($AvdName -notin $available) { throw "AVD $AvdName not found. Available: $($available -join ', ')" }
    Start-Process -FilePath $emulator -ArgumentList @(
        '-avd', $AvdName, '-no-snapshot-load', '-dns-server', '8.8.8.8',
        '-gpu', 'swiftshader_indirect', '-no-audio'
    ) | Out-Null
}

$ready = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    $booted = & $adb shell getprop sys.boot_completed 2>$null
    if ($LASTEXITCODE -eq 0 -and $booted.Trim() -eq '1') { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'Android TV emulator did not finish booting within two minutes.' }

& $adb install -r (Resolve-Path -LiteralPath $ApkPath).Path
if ($LASTEXITCODE -ne 0) { throw 'APK installation failed.' }
& $adb shell am force-stop com.ambienttv.photosambient
& $adb shell monkey -p com.ambienttv.photosambient -c android.intent.category.LEANBACK_LAUNCHER 1 | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'App launch failed.' }

Write-Host 'Android TV emulator is running. Use the emulator window with arrow keys, Enter, and Escape.'
Write-Host "Media response counts: & '$adb' logcat -s AmbientMediaProbe:I '*:S'"
Write-Host 'APK updates use adb install -r and preserve the saved Google session. Do not clear app data between tests.'
