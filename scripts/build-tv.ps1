param(
    [Parameter(Mandatory = $true)]
    [string]$OAuthJsonPath
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$credential = (Get-Content -LiteralPath $OAuthJsonPath -Raw | ConvertFrom-Json).installed
if (-not $credential.client_id -or -not $credential.client_secret) {
    throw 'Expected a Google TV/limited-input OAuth client JSON with installed.client_id and installed.client_secret.'
}

# Pass credentials only through this build process. The generated APK necessarily
# contains the TV OAuth client values; never publish it as a general release.
$env:AMBIENT_CLIENT_ID = $credential.client_id
$env:AMBIENT_CLIENT_SECRET = $credential.client_secret
try {
    Push-Location $projectRoot
    try {
        & .\gradlew.bat :app:assembleDebug --console=plain
        if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE." }
        $apk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
        Write-Host "APK: $apk"
    } finally {
        Pop-Location
    }
} finally {
    Remove-Item Env:AMBIENT_CLIENT_ID, Env:AMBIENT_CLIENT_SECRET -ErrorAction SilentlyContinue
}
