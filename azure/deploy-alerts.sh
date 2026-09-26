#!/usr/bin/env bash
set -e

# Usage: ./deploy-alerts.sh <resource-group> <action-group-name> <webhook-url>
# Requires: az cli logged in (az login)

RG="$1"
AG_NAME="$2"
WEBHOOK_URL="$3"

if [ -z "$RG" ] || [ -z "$AG_NAME" ] || [ -z "$WEBHOOK_URL" ]; then
  echo "Usage: $0 <resource-group> <action-group-name> <webhook-url>"
  exit 1
fi

az deployment group create \
  --resource-group "$RG" \
  --template-file "$(dirname "$0")/alert-actiongroup-template.json" \
  --parameters actionGroupName="$AG_NAME" resourceGroup="$RG" webhookUrl="$WEBHOOK_URL"

echo "Action Group deployed: $AG_NAME"

echo "Next: create an Activity Log alert that uses this Action Group (see README)."

exit 0
