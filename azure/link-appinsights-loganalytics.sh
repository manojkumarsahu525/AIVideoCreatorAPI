#!/usr/bin/env bash
set -e

# Usage: ./link-appinsights-loganalytics.sh <resource-group> <appinsights-name> <log-analytics-workspace-resource-id>
# Example workspace resource id: /subscriptions/<sub>/resourceGroups/<rg>/providers/Microsoft.OperationalInsights/workspaces/<name>

RG="$1"
APPINSIGHTS_NAME="$2"
WORKSPACE_RESOURCE_ID="$3"

if [ -z "$RG" ] || [ -z "$APPINSIGHTS_NAME" ] || [ -z "$WORKSPACE_RESOURCE_ID" ]; then
  echo "Usage: $0 <resource-group> <appinsights-name> <log-analytics-workspace-resource-id>"
  exit 1
fi

SUBSCRIPTION_ID=$(az account show --query id -o tsv)
APPINSIGHTS_RESOURCE_ID="/subscriptions/${SUBSCRIPTION_ID}/resourceGroups/${RG}/providers/microsoft.insights/components/${APPINSIGHTS_NAME}"

echo "Linking Application Insights ($APPINSIGHTS_RESOURCE_ID) to Log Analytics workspace: $WORKSPACE_RESOURCE_ID"

az monitor diagnostic-settings create \
  --resource "$APPINSIGHTS_RESOURCE_ID" \
  --name "AppInsightsToLogAnalytics" \
  --workspace "$WORKSPACE_RESOURCE_ID" \
  --logs '[{"category":"AppServiceConsoleLogs","enabled":true},{"category":"AppServiceAppLogs","enabled":true}]' \
  --metrics '[{"category":"AllMetrics","enabled":true}]'

echo "Diagnostic settings created. App Insights telemetry will be available in Log Analytics workspace."

exit 0
