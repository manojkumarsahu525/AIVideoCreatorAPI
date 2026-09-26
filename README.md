# AIVideoCreatorAPI

This repository contains two components:

- AIVideoCreatorAPI: ASP.NET Core Web API (Net 8) that generates AI videos and uploads them to Azure Blob Storage.
- AndroidClient: Android app (Jetpack Compose) that calls the backend, handles authentication via Firebase Auth, and plays generated videos with ExoPlayer.

## Contents

- `AIVideoCreatorAPI/` - .NET 8 Web API project
- `AndroidClient/` - Android app module (Kotlin, Compose)

## Backend (AIVideoCreatorAPI)

Prerequisites
- .NET 8 SDK
- An Azure Storage account (for blobs)

Config
- Configure the Azure Blob Storage connection string using configuration or environment variable:
  - `AzureBlobStorage:ConnectionString`

- Configure AI provider settings (example keys):
  - `AiVideo:Endpoint` (POST endpoint for generation)
  - `AiVideo:BaseUrl` (optional base URL for named HttpClient)
  - `AiVideo:ApiKey` (optional bearer token)
  - `AiVideo:GenerationTimeoutMinutes` (optional)
  - `AiVideo:PollIntervalMs` (optional)

Security
- Do NOT commit secrets. Use environment variables or user secrets in development.

Run
1. From the `AIVideoCreatorAPI` folder run:
   - `dotnet restore`
   - `dotnet run`
2. Swagger UI is available in Development mode (by default) at `/swagger`.

API
- POST `/api/video/generate` with JSON body `{ "prompt": "your text" }`.
- Response: `{ "videoUrl": "https://..." }` (URL to uploaded blob)

Notes
- The `VideoService` will call the configured AI provider, poll for completion, download the generated video stream, and upload to the `videos` blob container (created if missing).

## Android client (AndroidClient)

Prerequisites
- Android Studio
- Android SDK (minSdk 21+)

Setup
1. Open the `AndroidClient` project in Android Studio.
2. Replace `AndroidClient/app/google-services.json` with your Firebase project file.
3. Update the backend base URL in `MainActivity.kt` (or `RetrofitClient`) to point to your running backend (include trailing slash). For emulator use `http://10.0.2.2:5001/` when backend runs on localhost.
4. Sync Gradle.

Features
- Login and Registration screens using Firebase Auth (`email` / `password`).
- After login, navigate to Video Generator screen.
- Enter prompt, call backend, show progress and errors.
- Play resulting video URL with ExoPlayer.
- Logout button signs out via `FirebaseAuth.getInstance().signOut()`.

Permissions
- `INTERNET` is required (declared in `AndroidManifest.xml`).

Dependencies
- Retrofit + Gson
- Coroutines
- Jetpack Compose
- Lifecycle/ViewModel KTX
- ExoPlayer
- Firebase Auth

## Testing & Development tips
- Use emulator network address `10.0.2.2` to access a backend running on host machine.
- Use Storage account SAS or RBAC in production; avoid public containers unless intended.
- Tune AI provider polling and timeout settings via configuration.

## Troubleshooting
- If blob uploads fail, check `AzureBlobStorage:ConnectionString` and networking.
- If AI calls fail, verify `AiVideo` settings and API key.
- For Firebase issues, verify `google-services.json` matches package name and SHA-1 if required.

## License
This workspace contains example/demo code. Adjust for production use and review security considerations before deployment.
