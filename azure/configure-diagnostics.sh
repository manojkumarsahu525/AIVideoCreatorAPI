#!/usr/bin/env bash
set -e

# Usage: ./configure-diagnostics.sh <resource-group> <webapp-name> <log-analytics-workspace-id>
# The workspace id is the resource id of the Log Analytics workspace (e.g. /subscriptions/.../resourceGroups/.../providers/Microsoft.OperationalInsights/workspaces/<name>)

RG="$1"
WEBAPP="$2"
WORKSPACE_RESOURCE_ID="$3"

if [ -z "$RG" ] || [ -z "$WEBAPP" ] || [ -z "$WORKSPACE_RESOURCE_ID" ]; then
  echo "Usage: $0 <resource-group> <webapp-name> <log-analytics-workspace-resource-id>"
  exit 1
fi

SUBSCRIPTION_ID=$(az account show --query id -o tsv)
TARGET_RESOURCE_ID="/subscriptions/${SUBSCRIPTION_ID}/resourceGroups/${RG}/providers/Microsoft.Web/sites/${WEBAPP}"

echo "Configuring diagnostic settings for $TARGET_RESOURCE_ID to send to Log Analytics workspace: $WORKSPACE_RESOURCE_ID"

az monitor diagnostic-settings create \
  --resource "$TARGET_RESOURCE_ID" \
  --name "sendToLogAnalyticsForAutoscale" \
  --workspace "$WORKSPACE_RESOURCE_ID" \
  --logs '[{"category": "AppServiceHttpLogs", "enabled": true}, {"category": "AppServiceConsoleLogs", "enabled": true}, {"category": "AppServiceAppLogs", "enabled": true}]' \
  --metrics '[{"category": "AllMetrics", "enabled": true}]'

echo "Diagnostic settings created. App Service metrics and logs will be sent to Log Analytics.\nYou can query autoscale events from the Activity Log or from AzureActivity in Log Analytics." 

exit 0
