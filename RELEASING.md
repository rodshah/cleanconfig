# Release Guide

## Quick Start: Release a New Version

### 1. Test Locally (Optional)
```bash
./gradlew clean build test
```

### 2. Release via GitHub Actions

**Option A: Push a Tag**
```bash
# Update version (remove -SNAPSHOT)
vim gradle.properties  # Change 0.1.0-SNAPSHOT -> 0.1.0

git commit -am "chore: release 0.1.0"
git tag -a v0.1.0 -m "Release 0.1.0"
git push origin main --tags
```

**Option B: Manual Trigger**
- Go to: https://github.com/rodshah/cleanconfig/actions/workflows/release.yml
- Click "Run workflow"
- Enter version: `0.1.0`

### 3. Verify Release
- Check deployments: https://central.sonatype.com/publishing/deployments
- Search after 15 mins: https://central.sonatype.com/search?q=com.cleanconfig

## GitHub Secrets Required

Add these at https://github.com/rodshah/cleanconfig/settings/secrets/actions:

- `MAVEN_CENTRAL_USERNAME` - From https://central.sonatype.com/account
- `MAVEN_CENTRAL_PASSWORD` - From https://central.sonatype.com/account
- `GPG_PRIVATE_KEY` - Your GPG key (see below)
- `GPG_PASSPHRASE` - Your GPG passphrase

## GPG Key Export for GitHub

```bash
# Get your key ID
gpg --list-secret-keys --keyid-format=LONG

# Export for GitHub (with actual newlines)
gpg --armor --export-secret-keys YOUR_KEY_ID | pbcopy

# Paste this into GitHub secret GPG_PRIVATE_KEY
```

## Local Publishing (Alternative)

```bash
# Ensure ~/.gradle/gradle.properties has:
# mavenCentralUsername=<token>
# mavenCentralPassword=<token>
# signingKey=<gpg-key-as-single-line-with-\n>
# signingPassword=<gpg-passphrase>

./gradlew publishToMavenCentral
```

## Troubleshooting

**Build fails with metadata error:**
- Ensure `description` is set as project property, not in metadata block

**Signing fails:**
- Local: Check `signingKey` format in ~/.gradle/gradle.properties
- CI: Verify GPG_PRIVATE_KEY secret contains full key with headers

**Authentication fails:**
- Regenerate token at https://central.sonatype.com/account
- Update credentials in ~/.gradle/gradle.properties or GitHub secrets
