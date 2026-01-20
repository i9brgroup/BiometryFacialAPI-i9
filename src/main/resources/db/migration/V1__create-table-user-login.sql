-- Migration: Create biometric_login table
-- Description: Creates the main user authentication table with biometric login support
-- Target: SQL Server

-- Create the biometric_login table
CREATE TABLE biometric_login (
    id BIGINT IDENTITY(1,1) NOT NULL,
    username NVARCHAR(255) NULL,
    email NVARCHAR(255) NOT NULL,
    password NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    site_id NVARCHAR(255) NOT NULL,
    roles NVARCHAR(500) NULL,

    CONSTRAINT PK_biometric_login PRIMARY KEY CLUSTERED (id),
    CONSTRAINT UQ_biometric_login_email UNIQUE (email)
);

-- Create index on site_id for faster queries filtering by site
CREATE NONCLUSTERED INDEX IX_biometric_login_site_id
    ON biometric_login(site_id);

-- Create index on username for faster authentication queries
CREATE NONCLUSTERED INDEX IX_biometric_login_username
    ON biometric_login(username)
    WHERE username IS NOT NULL;

-- Create index on created_at for temporal queries
CREATE NONCLUSTERED INDEX IX_biometric_login_created_at
    ON biometric_login(created_at DESC);
