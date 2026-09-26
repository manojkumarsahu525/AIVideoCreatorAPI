#!/usr/bin/env bash
set -e

# Usage: ./deploy-activitylog-alert.sh <resource-group> <alert-name> <scope-resource-id> <action-group-resource-id>
# Requires: az cli logged in (az login)

RG="$1"
ALERT_NAME="$2"
SCOPE_RESOURCE_ID="$3"
ACTION_GROUP_ID="$4"

if [ -z "$RG" ] || [ -z "$ALERT_NAME" ] || [ -z "$SCOPE_RESOURCE_ID" ] || [ -z "$ACTION_GROUP_ID" ]; then
  echo "Usage: $0 <resource-group> <alert-name> <scope-resource-id> <action-group-resource-id>"
  exit 1
fi

az deployment group create \
  --resource-group "$RG" \
  --template-file "$(dirname "$0")/activitylog-alert-template.json" \
  --parameters alertName="$ALERT_NAME" scopeResourceId="$SCOPE_RESOURCE_ID" actionGroupId="$ACTION_GROUP_ID"

echo "Activity Log alert deployed: $ALERT_NAME"

exit 0
