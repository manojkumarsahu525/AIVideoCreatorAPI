#!/usr/bin/env bash
set -e

# Usage: ./create-ef-migration.sh <migration-name>
# This script creates an EF Core migration for the AIVideoCreatorAPI project.

MIGRATION_NAME=${1:-InitialCreate}

echo "Creating EF Core migration: $MIGRATION_NAME"
cd AIVideoCreatorAPI

if ! command -v dotnet-ef >/dev/null 2>&1; then
  echo "dotnet-ef tool not found. Installing dotnet-ef global tool..."
  dotnet tool install --global dotnet-ef || echo "dotnet-ef may already be installed"
fi

dotnet ef migrations add "$MIGRATION_NAME" --project AIVideoCreatorAPI --output-dir Migrations

echo "Migration created. To apply run: dotnet ef database update --project AIVideoCreatorAPI"

exit 0
