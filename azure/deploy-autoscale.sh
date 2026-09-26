#!/usr/bin/env bash
set -e

# Usage: ./deploy-autoscale.sh <resource-group> <webapp-name> <autoscale-name>
# Requires: az cli logged in (az login)

RG="$1"
WEBAPP="$2"
AUTOSCALE_NAME="$3"

if [ -z "$RG" ] || [ -z "$WEBAPP" ] || [ -z "$AUTOSCALE_NAME" ]; then
  echo "Usage: $0 <resource-group> <webapp-name> <autoscale-name>"
  exit 1
fi

SUBSCRIPTION_ID=$(az account show --query id -o tsv)
if [ -z "$SUBSCRIPTION_ID" ]; then
  echo "Unable to determine subscription id. Are you logged in?"
  exit 1
fi

TARGET_RESOURCE_ID="/subscriptions/${SUBSCRIPTION_ID}/resourceGroups/${RG}/providers/Microsoft.Web/sites/${WEBAPP}"

echo "Deploying autoscale settings '$AUTOSCALE_NAME' for resource: $TARGET_RESOURCE_ID"

az deployment group create \
  --resource-group "$RG" \
  --template-file "$(dirname "$0")/autoscale-template.json" \
  --parameters autoscaleSettingName="$AUTOSCALE_NAME" targetResourceUri="$TARGET_RESOURCE_ID" minCapacity=1 maxCapacity=5 defaultCapacity=1

echo "Autoscale deployment initiated."

exit 0
