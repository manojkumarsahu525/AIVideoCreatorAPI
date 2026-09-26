#!/usr/bin/env bash
set -e

# Usage: ./deploy-alert-metrics.sh <resource-group> <alert-name> <app-insights-resource-id>
RG="$1"
ALERT_NAME="$2"
TARGET_RESOURCE_ID="$3"

if [ -z "$RG" ] || [ -z "$ALERT_NAME" ] || [ -z "$TARGET_RESOURCE_ID" ]; then
  echo "Usage: $0 <resource-group> <alert-name> <app-insights-resource-id>"
  exit 1
fi

az deployment group create \
  --resource-group "$RG" \
  --template-file "$(dirname "$0")/alerts-metrics-template.json" \
  --parameters alertRuleName="$ALERT_NAME" targetResourceId="$TARGET_RESOURCE_ID"

echo "Alert metric rule deployed: $ALERT_NAME"

exit 0
