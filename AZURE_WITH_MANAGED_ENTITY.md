# Azure App Service → Azure SQL using Managed Identity (No IP Whitelisting)

This document explains how to securely connect an **Azure App Service** to **Azure SQL Database** using **Microsoft Entra ID (Managed Identity)** and **remove IP whitelisting, usernames, and passwords**.

---

## Problem Statement

Initially, the Azure SQL Server was accessed by:
- Whitelisting public IP addresses
- Using SQL username and password

This approach is **not recommended** because:
- IPs can change
- Secrets can leak
- Manual maintenance is required
- It is not Zero Trust

---

## Target Architecture

- App Service authenticates using **Managed Identity**
- Azure SQL authenticates using **Microsoft Entra ID**
- No SQL username/password
- No IP-based whitelisting

---

## Step 1: Update Database Connection URL

Update the JDBC URL to use **Managed Identity authentication**.

```text
jdbc:sqlserver://school.database.windows.net:1433;
database=school;
authentication=ActiveDirectoryManagedIdentity
```

## Step 2 - Update New Configuration

```
spring:
  datasource:
    url: ${sqldburl}
```

## Step 3: Enable Managed Identity
 - Go to App Service → Identity
- Enable System Assigned Managed Identity
- Go to SQL Server → Microsoft Entra ID
-Set a Microsoft Entra Admin
- This admin will be used to create identity-based users

## Step 4: Create Database User for App Service Identity

```
CREATE USER [learningdev1s] FROM EXTERNAL PROVIDER;
ALTER ROLE db_datareader ADD MEMBER [learningdev1s];
ALTER ROLE db_datawriter ADD MEMBER [learningdev1s];
```
You will face the below error 

Failed to execute query.
Principal 'learningdev1s' could not be created.
Only connections established with Active Directory accounts
can create other Active Directory users.

To make it work  you need to enable this check in SQL Server - Microsoft Entra  Id
Support only Microsoft Entra authentication for this server.
This disables SQL username/password login completely.
Run the command.

## Conclusion
- No IP whitelisting required
- No database credentials stored
- App authenticates using Managed Identity
- Azure SQL secured with Entra ID only

