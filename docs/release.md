# Local Build And Release

## What Is Still Missing

Before the app is truly testable on device, these are still incomplete:

- Real MediaPipe face/pose inference
- Real TFLite person/body-part segmentation
- Real OpenGL compositing for face/body swap
- Recorder surface integration for composed video export
- Android signing config for release APK/AAB

You can still build the scaffold and install debug builds locally once dependencies are installed.

## Build APK Locally

Quick checks:

```bash
./scripts/check-env.sh
./scripts/setup-android-env.sh
```

From `mobile/`:

```bash
npm install
```

Then from `mobile/android/`:

```bash
gradle wrapper
./gradlew assembleDebug
```

Debug APK output:

```text
mobile/android/app/build/outputs/apk/debug/app-debug.apk
```

Install on a connected device:

```bash
./scripts/install-apk.sh
```

Manual equivalent:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If the package is already installed and signing changes later:

```bash
adb uninstall com.overlay.mr
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Release APK

Create signing config first:

1. Generate a keystore
2. Add `MYAPP_UPLOAD_STORE_FILE`, `MYAPP_UPLOAD_KEY_ALIAS`, `MYAPP_UPLOAD_STORE_PASSWORD`, and `MYAPP_UPLOAD_KEY_PASSWORD`
3. Wire the signing config into `mobile/android/app/build.gradle`

Then build:

```bash
cd mobile/android
./gradlew assembleRelease
```

## GitHub Setup

From the repo root:

```bash
git init
git add .
git commit -m "Initial mixed reality scaffold"
git branch -M main
git remote add origin git@github.com:YOUR_ORG/overlay-mr.git
git push -u origin main
```

If you prefer HTTPS:

```bash
git remote add origin https://github.com/YOUR_ORG/overlay-mr.git
```

## GCP Setup

Recommended services:

- Cloud Run for backend
- Cloud Storage for uploads/assets
- Artifact Registry or Container Registry
- Optional Firestore later

Required commands:

```bash
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com storage.googleapis.com
```

Deploy backend manually:

```bash
cd backend
gcloud builds submit \
  --config cloudbuild.yaml \
  --substitutions _SERVICE_NAME=overlay-backend,_REGION=us-central1,_AR_REPOSITORY=overlay-mr,_IMAGE_NAME=overlay-backend,_ASSET_BUCKET=overlay-mr-assets
```

## GitHub To GCP CI/CD

The workflow file is:

```text
.github/workflows/backend-cloud-run.yml
```

Set these GitHub repository secrets:

- `GCP_PROJECT_ID`
- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_SERVICE_ACCOUNT`

Set these GitHub repository variables:

- `GCP_REGION`
- `GCP_ARTIFACT_REGISTRY_REPOSITORY`
- `GCP_CLOUD_RUN_SERVICE`
- `GCP_IMAGE_NAME`
- `GCP_ASSET_BUCKET`

Recommended approach:

- Use Workload Identity Federation
- Avoid storing long-lived JSON service account keys in GitHub

Recommended values for this repo:

- `GCP_REGION=us-central1`
- `GCP_ARTIFACT_REGISTRY_REPOSITORY=overlay-mr`
- `GCP_CLOUD_RUN_SERVICE=overlay-backend`
- `GCP_IMAGE_NAME=overlay-backend`
- `GCP_ASSET_BUCKET=overlay-mr-assets`

## Current Repo Readiness

Ready now:

- Monorepo structure
- `.gitignore`
- backend Docker and Cloud Build config
- GitHub Actions workflow for backend deploy
- Android/RN source scaffold and docs

Still required on your machine before first successful APK build:

- run `npm install` in `mobile/`
- install Android SDK/NDK/JDK toolchain
- add required TFLite and MediaPipe assets
- resolve any dependency/version drift during the first Gradle sync

## Local Capture Storage

Current app behavior is local-first:

- photo output is intended to be written under the app's Android pictures directory
- video output is intended to be written under the app's Android movies directory
- capture metadata can be kept locally in app storage before any optional backend sync
- backend upload is optional and should happen only if you explicitly call upload flows later
