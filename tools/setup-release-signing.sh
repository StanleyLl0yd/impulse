#!/usr/bin/env bash
set -euo pipefail

REPO="${1:-StanleyLl0yd/impulse}"
KEY_ALIAS="impulse"
KEY_DIR="${IMPULSE_KEY_DIR:-$HOME/.impulse-release}"
KEYSTORE="$KEY_DIR/impulse-release.jks"
CREDENTIALS="$KEY_DIR/impulse-release-credentials.txt"

gh auth status >/dev/null
command -v keytool >/dev/null
command -v openssl >/dev/null

mkdir -p "$KEY_DIR"
chmod 700 "$KEY_DIR"

if [[ -e "$KEYSTORE" || -e "$CREDENTIALS" ]]; then
  echo "Signing files already exist in $KEY_DIR. Refusing to overwrite them." >&2
  exit 1
fi

PASSWORD="$(openssl rand -base64 36 | tr -d '\n')"

keytool -genkeypair \
  -keystore "$KEYSTORE" \
  -storetype PKCS12 \
  -storepass "$PASSWORD" \
  -keypass "$PASSWORD" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -dname "CN=IMPULSE Release,O=StanleyLl0yd" >/dev/null

FINGERPRINT="$(
  keytool -list -v -J-Duser.language=en \
    -keystore "$KEYSTORE" \
    -storepass "$PASSWORD" \
    -alias "$KEY_ALIAS" |
  sed -n 's/.*SHA256: //p' |
  head -n 1 |
  tr -d ':' |
  tr '[:upper:]' '[:lower:]'
)"

test -n "$FINGERPRINT"
chmod 600 "$KEYSTORE"

cat > "$CREDENTIALS" <<CREDS
Repository: $REPO
Keystore: $KEYSTORE
Alias: $KEY_ALIAS
Store/key password: $PASSWORD
Certificate SHA-256: $FINGERPRINT
CREDS
chmod 600 "$CREDENTIALS"

KEYSTORE_BASE64="$(base64 < "$KEYSTORE" | tr -d '\n')"

gh api --method PUT "repos/$REPO/environments/release" >/dev/null
printf '%s' "$KEYSTORE_BASE64" | gh secret set ANDROID_KEYSTORE_BASE64 --env release --repo "$REPO"
printf '%s' "$PASSWORD" | gh secret set ANDROID_KEYSTORE_PASSWORD --env release --repo "$REPO"
printf '%s' "$KEY_ALIAS" | gh secret set ANDROID_KEY_ALIAS --env release --repo "$REPO"
printf '%s' "$PASSWORD" | gh secret set ANDROID_KEY_PASSWORD --env release --repo "$REPO"
printf '%s' "$FINGERPRINT" | gh secret set ANDROID_CERT_SHA256 --env release --repo "$REPO"

unset PASSWORD KEYSTORE_BASE64

echo "Dedicated IMPULSE release key created and GitHub release secrets configured."
echo "Keystore: $KEYSTORE"
echo "Credentials: $CREDENTIALS"
echo "Certificate SHA-256: $FINGERPRINT"
echo "Back up both local files securely before publishing the first store release."
