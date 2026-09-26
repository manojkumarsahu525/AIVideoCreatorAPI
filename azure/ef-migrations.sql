IF OBJECT_ID(N'[__EFMigrationsHistory]') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END;
GO

BEGIN TRANSACTION;
GO

DECLARE @var0 sysname;
SELECT @var0 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[VideoRecords]') AND [c].[name] = N'UserId');
IF @var0 IS NOT NULL EXEC(N'ALTER TABLE [VideoRecords] DROP CONSTRAINT [' + @var0 + '];');
UPDATE [VideoRecords] SET [UserId] = '' WHERE [UserId] IS NULL;
ALTER TABLE [VideoRecords] ALTER COLUMN [UserId] TEXT NOT NULL;
ALTER TABLE [VideoRecords] ADD DEFAULT '' FOR [UserId];
GO

DECLARE @var1 sysname;
SELECT @var1 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[VideoRecords]') AND [c].[name] = N'Prompt');
IF @var1 IS NOT NULL EXEC(N'ALTER TABLE [VideoRecords] DROP CONSTRAINT [' + @var1 + '];');
UPDATE [VideoRecords] SET [Prompt] = '' WHERE [Prompt] IS NULL;
ALTER TABLE [VideoRecords] ALTER COLUMN [Prompt] TEXT NOT NULL;
ALTER TABLE [VideoRecords] ADD DEFAULT '' FOR [Prompt];
GO

DECLARE @var2 sysname;
SELECT @var2 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[VideoRecords]') AND [c].[name] = N'BlobUrl');
IF @var2 IS NOT NULL EXEC(N'ALTER TABLE [VideoRecords] DROP CONSTRAINT [' + @var2 + '];');
UPDATE [VideoRecords] SET [BlobUrl] = '' WHERE [BlobUrl] IS NULL;
ALTER TABLE [VideoRecords] ALTER COLUMN [BlobUrl] TEXT NOT NULL;
ALTER TABLE [VideoRecords] ADD DEFAULT '' FOR [BlobUrl];
GO

DECLARE @var3 sysname;
SELECT @var3 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[UserSubscriptions]') AND [c].[name] = N'UserId');
IF @var3 IS NOT NULL EXEC(N'ALTER TABLE [UserSubscriptions] DROP CONSTRAINT [' + @var3 + '];');
UPDATE [UserSubscriptions] SET [UserId] = '' WHERE [UserId] IS NULL;
ALTER TABLE [UserSubscriptions] ALTER COLUMN [UserId] TEXT NOT NULL;
ALTER TABLE [UserSubscriptions] ADD DEFAULT '' FOR [UserId];
GO

DECLARE @var4 sysname;
SELECT @var4 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[UserSubscriptions]') AND [c].[name] = N'StripeSubscriptionId');
IF @var4 IS NOT NULL EXEC(N'ALTER TABLE [UserSubscriptions] DROP CONSTRAINT [' + @var4 + '];');
UPDATE [UserSubscriptions] SET [StripeSubscriptionId] = '' WHERE [StripeSubscriptionId] IS NULL;
ALTER TABLE [UserSubscriptions] ALTER COLUMN [StripeSubscriptionId] TEXT NOT NULL;
ALTER TABLE [UserSubscriptions] ADD DEFAULT '' FOR [StripeSubscriptionId];
GO

DECLARE @var5 sysname;
SELECT @var5 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[UserSubscriptions]') AND [c].[name] = N'StripeCustomerId');
IF @var5 IS NOT NULL EXEC(N'ALTER TABLE [UserSubscriptions] DROP CONSTRAINT [' + @var5 + '];');
UPDATE [UserSubscriptions] SET [StripeCustomerId] = '' WHERE [StripeCustomerId] IS NULL;
ALTER TABLE [UserSubscriptions] ALTER COLUMN [StripeCustomerId] TEXT NOT NULL;
ALTER TABLE [UserSubscriptions] ADD DEFAULT '' FOR [StripeCustomerId];
GO

DECLARE @var6 sysname;
SELECT @var6 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[UserSubscriptions]') AND [c].[name] = N'Status');
IF @var6 IS NOT NULL EXEC(N'ALTER TABLE [UserSubscriptions] DROP CONSTRAINT [' + @var6 + '];');
UPDATE [UserSubscriptions] SET [Status] = '' WHERE [Status] IS NULL;
ALTER TABLE [UserSubscriptions] ALTER COLUMN [Status] TEXT NOT NULL;
ALTER TABLE [UserSubscriptions] ADD DEFAULT '' FOR [Status];
GO

DECLARE @var7 sysname;
SELECT @var7 = [d].[name]
FROM [sys].[default_constraints] [d]
INNER JOIN [sys].[columns] [c] ON [d].[parent_column_id] = [c].[column_id] AND [d].[parent_object_id] = [c].[object_id]
WHERE ([d].[parent_object_id] = OBJECT_ID(N'[UserSubscriptions]') AND [c].[name] = N'Plan');
IF @var7 IS NOT NULL EXEC(N'ALTER TABLE [UserSubscriptions] DROP CONSTRAINT [' + @var7 + '];');
UPDATE [UserSubscriptions] SET [Plan] = '' WHERE [Plan] IS NULL;
ALTER TABLE [UserSubscriptions] ALTER COLUMN [Plan] TEXT NOT NULL;
ALTER TABLE [UserSubscriptions] ADD DEFAULT '' FOR [Plan];
GO

INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
VALUES (N'20260926074936_InitialSqlServer', N'8.0.8');
GO

COMMIT;
GO

