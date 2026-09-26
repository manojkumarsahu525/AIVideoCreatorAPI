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

Firebase service account (server-side)
- To verify Firebase ID tokens on the backend you must provide Firebase service account credentials to the server.
- Recommended way (Firebase Console):
  1. Go to Firebase Console -> Project Settings -> Service accounts.
  2. Click "Generate new private key" and download the JSON key file.
  3. Store the JSON on the server (do NOT commit it to source control).

Configuration options
- Option A (environment variable - recommended for CI/hosting):
  - Set `GOOGLE_APPLICATION_CREDENTIALS` to the full path of the downloaded JSON file. The Firebase Admin SDK will use Application Default Credentials.
- Option B (appsettings):
  - Add to `appsettings.json` or environment variables:

```json
"Firebase": {
  "CredentialFilePath": "C:\\path\\to\\service-account.json",
  "ProjectId": "your-firebase-project-id"
}
```

- The backend supports both methods. If `Firebase:CredentialFilePath` is set the app will load credentials from that file; otherwise it falls back to Application Default Credentials.

Security notes
- Keep the service account JSON private. Use environment variables or secret stores (Key Vault, Azure App Configuration) in production.
- Prefer using workload identities or managed identities on cloud hosts instead of long-lived service account keys when possible.

Client usage (Android)
- After signing in on the Android client, obtain the ID token and include it in requests to the backend as an Authorization header:

```kotlin
FirebaseAuth.getInstance().getCurrentUser()?.getIdToken(true)?.addOnSuccessListener { result ->
    val idToken = result.token
    // Send header: "Authorization: Bearer $idToken"
}
```

The backend validates the token using the Firebase Admin SDK and extracts the user's UID for auditing.

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

Stripe / Payments (backend configuration example)

Add Stripe keys and price ids to `AIVideoCreatorAPI/appsettings.Development.json` (example already provided).

Example snippet (already present in `appsettings.Development.json`):

```json
"Stripe": {
  "SecretKey": "sk_test_your_secret_key",
  "PublishableKey": "pk_test_your_publishable_key",
  "WebhookSecret": "whsec_your_webhook_secret",
  "SuccessUrl": "https://your-app/success",
  "CancelUrl": "https://your-app/cancel",
  "Prices": {
    "basic": "price_basic_id",
    "pro": "price_pro_id"
  }
}
```

Client-side (Android) usage example — create checkout session and open URL:

```kotlin
// Call backend to create checkout session (include Firebase ID token in Authorization header)
val response = api.createCheckoutSession(CreateCheckoutRequest("basic", email, successUrl, cancelUrl))
if (response.isSuccessful) {
    val sessionUrl = response.body()?.sessionUrl
    // open browser or Custom Tab to sessionUrl
}
```

Send Firebase ID token in header when calling backend APIs:

```kotlin
FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
    val idToken = result.token
    // send header: Authorization: Bearer $idToken
}
```

## Troubleshooting
- If blob uploads fail, check `AzureBlobStorage:ConnectionString` and networking.
- If AI calls fail, verify `AiVideo` settings and API key.
- For Firebase issues, verify `google-services.json` matches package name and SHA-1 if required.
 - If you want, I can:
   - Add an example Activity Log alert + Action Group ARM template to call a webhook endpoint on scaling events.
   - Implement a simple webhook endpoint in this API to receive scale notifications and log them to Application Insights (requires exposing a public endpoint and configuring the Action Group webhook URL).

 I've added templates and scripts to deploy an Action Group (webhook) and an Activity Log autoscale deployment template. A basic webhook endpoint has been added to the API at `POST /api/scale/notify`.

 Activity Log alert + Action Group

 1. Deploy Action Group (webhook receiver):
    `./azure/deploy-alerts.sh <resource-group> <action-group-name> <webhook-url>`

 2. Create Activity Log Alert (via portal or ARM) targeting autoscale operations and attach the Action Group. The `azure/autoscale-template.json` can be used to create the autoscale settings; then attach the Action Group to an Activity Log alert for autoscale operations.

 Webhook endpoint

 - The API exposes `POST /api/scale/notify` which accepts webhook POSTs from Azure Action Groups. It logs the raw payload to Application Insights and server logs for auditing. The controller code is in `AIVideoCreatorAPI/Controllers/ScaleWebhookController.cs`.

 Security note
 - By default the webhook endpoint is unauthenticated (`AllowAnonymous`) so Azure can call it. For production, consider adding a shared secret or validating the request origin. You can configure the Action Group to include a custom header and validate it in the webhook handler.

## Autoscale (Azure App Service)

This repo includes scripts and an ARM template to configure App Service autoscaling and diagnostics.

Files:
- `azure/autoscale-template.json` - ARM template for `Microsoft.Insights/autoscalesettings` which creates CPU-based autoscale rules (scale-out at >70% for 5m, scale-in at <30% for 10m) with min 1 and max 5 instances.
- `azure/deploy-autoscale.sh` - Helper script to deploy the autoscale ARM template to a resource group for a specified App Service.
- `azure/configure-diagnostics.sh` - Helper script to configure diagnostic settings for the App Service to send logs and metrics to a Log Analytics workspace.

Quick deploy (Azure CLI):

1. Login: `az login`
2. Deploy autoscale:
   `./azure/deploy-autoscale.sh <resource-group> <webapp-name> <autoscale-name>`

3. Configure diagnostics (optional, to send logs/metrics to Log Analytics):
   `./azure/configure-diagnostics.sh <resource-group> <webapp-name> <log-analytics-workspace-resource-id>`

Query scaling events (Log Analytics)

If you send Activity Logs / diagnostics to Log Analytics you can find autoscale-related events using KQL. Example:

```
AzureActivity
| where ResourceProviderValue contains "Microsoft.Insights" or OperationNameValue contains "Autoscale"
| sort by TimeGenerated desc
```

You can also query metrics in Application Insights / Metrics explorer for CPU and instance count changes.

Logging scaling events

Autoscale actions are emitted to the Azure Activity Log. By configuring diagnostic settings to route Activity Log events and App Service metrics to a Log Analytics workspace, you can create queries and alerts to notify you when scaling happens.

If you want application-level confirmation, consider adding an Azure Monitor Activity Log alert with an Action Group that calls a webhook under your control. That webhook can POST into your application or another endpoint that logs and correlates the event with application telemetry.
 - For Firebase issues, verify `google-services.json` matches package name and SHA-1 if required.

## Migrations and Azure SQL

To use a managed database in production, switch from the default SQLite to Azure SQL and apply EF Core migrations.

Quick steps (local development -> Azure SQL):

1. Update `AIVideoCreatorAPI/appsettings.json` or environment variable `ConnectionStrings__DefaultConnection` with the Azure SQL connection string.
2. Create a migration locally:

```
cd AIVideoCreatorAPI
dotnet tool install --global dotnet-ef
dotnet ef migrations add InitialCreate
dotnet ef database update
```

3. For production, run the migrations against your Azure SQL database or include migration execution on startup.

ARM template
- An ARM template is provided at `azure/azure-sql-template.json` to provision an Azure SQL server and database. It outputs a connection string you can use for the app.


## License
This workspace contains example/demo code. Adjust for production use and review security considerations before deployment.

## Analytics & Dashboards

This project includes Application Insights telemetry with custom events and scripts to export telemetry to Log Analytics and create alerts and dashboards.

What is tracked
- `VideoGenerationStarted` — properties: `uid`, `prompt`.
- `VideoGenerationCompleted` — properties: `uid`, `prompt`, `blobUrl`, `durationMs` (also tracks metric `VideoGenerationDurationMs`).
- `SubscriptionActivated`, `SubscriptionUpdated`, `SubscriptionPaymentFailed` — properties: `userId`, `plan`, `status`, `amountPaid` (when available); also tracks metric `SubscriptionRevenue` when invoice amount is present.

Export telemetry to Log Analytics
- Use `azure/link-appinsights-loganalytics.sh` to create diagnostic settings that route Application Insights telemetry and metrics into a Log Analytics workspace. This enables advanced queries and Power BI connectivity.

Power BI
- Connect Power BI to Log Analytics (Azure Monitor connector) or directly to Application Insights to build dashboards. Recommended: route App Insights to Log Analytics and connect Power BI to the workspace.

Dashboard/Kusto query examples
- Daily active users (unique users by day):

```
customEvents
| where timestamp >= ago(30d)
| where name == "VideoGenerationStarted"
| extend uid = tostring(customDimensions.uid)
| summarize dau = dcount(uid) by bin(timestamp, 1d)
| order by timestamp
```

- Videos generated per day:

```
customEvents
| where timestamp >= ago(30d)
| where name == "VideoGenerationCompleted"
| summarize videos = count() by bin(timestamp, 1d)
| order by timestamp
```

- Average video generation time (ms):

```
customMetrics
| where name == "VideoGenerationDurationMs"
| summarize avgDurationMs = avg(value) by bin(timestamp, 1d)
| order by timestamp
```

- Subscription revenue trends:

```
customMetrics
| where name == "SubscriptionRevenue"
| summarize revenue = sum(value) by bin(timestamp, 1d)
| order by timestamp
```

Alerts
- Example metric alerts template is provided in `azure/alerts-metrics-template.json` and can be deployed with `azure/deploy-alert-metrics.sh`.
- Suggested alerts:
  - High error rate (failed requests > threshold)
  - Slow average request duration (Request Duration > threshold)
  - Spike in VideoGenerationDurationMs

Application-level logging of scaling events and payments
- The API has endpoints and telemetry to capture scale notifications (`POST /api/scale/notify`) and subscription events (Stripe webhooks). These are emitted into Application Insights and can be correlated in Log Analytics dashboards.

If you want, I can add a sample Power BI report (PBIX template) or automated ARM templates to create dashboards and alerts in Azure.
