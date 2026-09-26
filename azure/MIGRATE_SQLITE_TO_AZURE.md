Migrating SQLite data to Azure SQL

This document provides sample steps and scripts to migrate existing local SQLite data (used by the development SQLite DB) to Azure SQL. The project schema includes `UserSubscriptions` and `VideoRecords` tables.

Overview
1. Export data from SQLite to CSV
2. Create target tables in Azure SQL (script provided: `azure/sqlserver-init.sql`)
3. Import CSV into Azure SQL using `bcp` or Azure Data Factory / Azure Data Studio

Prerequisites
- SQLite CLI (`sqlite3`) available locally
- `bcp` utility (provided with SQL Server tools) or Azure Data Factory / Azure Data Studio
- Azure SQL connection string and server admin credentials

Step A: Export CSV from SQLite

1. Locate your SQLite DB (default: `aivideocreator.db` in project root)
2. Export tables to CSV:

```bash
SQLITE_DB="./aivideocreator.db"
sqlite3 "$SQLITE_DB" \
  -cmd ".headers on" \
  -csv "SELECT * FROM UserSubscriptions;" > user_subscriptions.csv
sqlite3 "$SQLITE_DB" -csv "SELECT * FROM VideoRecords;" > video_records.csv
```

Note: if your sqlite3 doesn't support that inline, use:

```bash
sqlite3 -header -csv "$SQLITE_DB" "SELECT * FROM UserSubscriptions;" > user_subscriptions.csv
sqlite3 -header -csv "$SQLITE_DB" "SELECT * FROM VideoRecords;" > video_records.csv
```

Step B: Create tables in Azure SQL

Use the provided `azure/sqlserver-init.sql` script to create tables and indexes in your Azure SQL database. Run it via `sqlcmd` or in Azure Data Studio.

Example with `sqlcmd`:

```bash
sqlcmd -S tcp:<your-server>.database.windows.net,1433 -d <your-database> -U <username> -P '<password>' -i azure/sqlserver-init.sql
```

Step C: Import CSV into Azure SQL

Option 1: Use `bcp` from client machine

```bash
# import user_subscriptions
bcp <database>.dbo.UserSubscriptions in user_subscriptions.csv -S tcp:<server>.database.windows.net,1433 -U <username> -P '<password>' -c -t "," -F 2

# import video_records
bcp <database>.dbo.VideoRecords in video_records.csv -S tcp:<server>.database.windows.net,1433 -U <username> -P '<password>' -c -t "," -F 2
```

Notes:
- `-F 2` starts from second row to skip headers
- You may need to map CSV columns to table columns explicitly; use a format file if necessary.

Option 2: Upload CSV to Azure Blob Storage and use `BULK INSERT` or `OPENROWSET(BULK...)`

1. Upload CSV files to a container in a storage account (use SAS or managed identity)
2. Use `BULK INSERT` or `OPENROWSET(BULK ...)` in T-SQL referencing the blob URL (requires appropriate permissions and server config). Example:

```sql
-- needs admin and proper credential setup
BULK INSERT dbo.VideoRecords
FROM 'https://<storage-account>.blob.core.windows.net/<container>/video_records.csv'
WITH (DATA_SOURCE = 'MyBlobStorage', FORMAT='CSV', FIRSTROW = 2);
```

Option 3: Use Azure Data Factory

- Use Copy Activity: Source = Azure Blob (CSV), Sink = Azure SQL Database. This is the most robust approach for production migrations, supports mapping, transformations, and scheduling.

Verification
- Run a few queries to validate counts and sample rows:

```sql
SELECT COUNT(*) FROM dbo.VideoRecords;
SELECT TOP 10 * FROM dbo.VideoRecords ORDER BY CreatedAt DESC;
```

Tips
- If CSV fields contain commas or embedded newlines, ensure proper quoting when exporting from SQLite. Use a robust import (ADF or Azure Data Studio) if needed.
- Consider performing migration in a maintenance window and verifying referential integrity.

If you want, I can add a script that automates CSV export and pushes the files to an Azure storage account (requires `az` cli and storage account credentials).
