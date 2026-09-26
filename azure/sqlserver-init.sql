-- SQL Server initialization script for AIVideoCreatorAPI
-- Creates tables for UserSubscriptions and VideoRecords

CREATE TABLE dbo.UserSubscriptions (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    UserId NVARCHAR(200) NULL,
    Plan NVARCHAR(100) NULL,
    Status NVARCHAR(50) NULL,
    ExpiryDate DATETIME2 NULL,
    StripeCustomerId NVARCHAR(200) NULL,
    StripeSubscriptionId NVARCHAR(200) NULL
);

CREATE TABLE dbo.VideoRecords (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    UserId NVARCHAR(200) NULL,
    Prompt NVARCHAR(MAX) NULL,
    DurationMs FLOAT NOT NULL,
    BlobUrl NVARCHAR(2000) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

GO

-- Indexes for common queries
CREATE INDEX IDX_VideoRecords_UserId_CreatedAt ON dbo.VideoRecords(UserId, CreatedAt);
CREATE INDEX IDX_UserSubscriptions_UserId ON dbo.UserSubscriptions(UserId);
GO
