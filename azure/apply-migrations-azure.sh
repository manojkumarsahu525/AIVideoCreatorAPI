#!/usr/bin/env bash
set -e

# Usage: ./apply-migrations-azure.sh "<connection-string>"
# Requires: dotnet-ef installed and dotnet available

CONN="$1"
if [ -z "$CONN" ]; then
  echo "Usage: $0 \"<connection-string>\""
  exit 1
fi

cd AIVideoCreatorAPI
export ConnectionStrings__DefaultConnection="$CONN"

echo "Applying EF Core migrations using connection string from env var ConnectionStrings__DefaultConnection"

dotnet ef database update

echo "Migrations applied."

exit 0
