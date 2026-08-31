param(
    [string]$Repo = "StanleyLl0yd/impulse",
    [string]$KeyDirectory = "$HOME\.impulse-release"
)

$ErrorActionPreference = "Stop"
if ($PSVersionTable.PSVersion.Major -ge 7) {
    $PSNativeCommandUseErrorActionPreference = $true
}
$KeyAlias = "impulse"
$Keystore = Join-Path $KeyDirectory "impulse-release.jks"
$Credentials = Join-Path $KeyDirectory "impulse-release-credentials.txt"

gh auth status | Out-Null
if (-not (Get-Command keytool -ErrorAction SilentlyContinue)) {
    throw "keytool was not found. Install a JDK first."
}

New-Item -ItemType Directory -Force -Path $KeyDirectory | Out-Null
if ((Test-Path $Keystore) -or (Test-Path $Credentials)) {
    throw "Signing files already exist in $KeyDirectory. Refusing to overwrite them."
}

$random = New-Object byte[] 36
[System.Security.Cryptography.RandomNumberGenerator]::Fill($random)
$Password = [Convert]::ToBase64String($random)

& keytool -genkeypair `
    -keystore $Keystore `
    -storetype PKCS12 `
    -storepass $Password `
    -keypass $Password `
    -alias $KeyAlias `
    -keyalg RSA `
    -keysize 4096 `
    -validity 10000 `
    -dname "CN=IMPULSE Release,O=StanleyLl0yd" | Out-Null

$keytoolOutput = & keytool -list -v -J-Duser.language=en -keystore $Keystore -storepass $Password -alias $KeyAlias
$fingerprintLine = $keytoolOutput | Where-Object { $_ -match "SHA256:" } | Select-Object -First 1
if (-not $fingerprintLine) {
    throw "Could not read the release certificate SHA-256 fingerprint."
}
$Fingerprint = (($fingerprintLine -replace '^.*SHA256:\s*', '') -replace ':', '').ToLowerInvariant()

@"
Repository: $Repo
Keystore: $Keystore
Alias: $KeyAlias
Store/key password: $Password
Certificate SHA-256: $Fingerprint
"@ | Set-Content -Encoding UTF8 $Credentials

$KeystoreBase64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($Keystore))

gh api --method PUT "repos/$Repo/environments/release" | Out-Null
gh secret set ANDROID_KEYSTORE_BASE64 --env release --repo $Repo --body $KeystoreBase64
gh secret set ANDROID_KEYSTORE_PASSWORD --env release --repo $Repo --body $Password
gh secret set ANDROID_KEY_ALIAS --env release --repo $Repo --body $KeyAlias
gh secret set ANDROID_KEY_PASSWORD --env release --repo $Repo --body $Password
gh secret set ANDROID_CERT_SHA256 --env release --repo $Repo --body $Fingerprint

$Password = $null
$KeystoreBase64 = $null

Write-Host "Dedicated IMPULSE release key created and GitHub release secrets configured."
Write-Host "Keystore: $Keystore"
Write-Host "Credentials: $Credentials"
Write-Host "Certificate SHA-256: $Fingerprint"
Write-Host "Back up both local files securely before publishing the first store release."
