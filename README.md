# Spring Boot Application Deployment on Azure using CI/CD

This is a simple project demonstrating how to deploy a **Spring Boot** application on **Azure** using:
- Azure SQL Database
- Azure Key Vault for secret management
- Azure Container Registry (ACR)
- Azure App Service (Container-based)
- GitHub Actions for CI/CD automation

---

## Tech Stack

- **Backend**: Spring Boot
- **Database**: Azure SQL
- **Containerization**: Docker
- **Registry**: Azure Container Registry (ACR)
- **Secrets**: Azure Key Vault
- **CI/CD**: GitHub Actions
- **Cloud Provider**: Microsoft Azure

---

## Build the Application

First, package the Spring Boot application:

```bash
mvn clean package
```

## Azure Prerequisites

Before deploying the application, ensure the following prerequisites are met:

### IAM Roles
- The user performing the deployment must have **Contributor** (or higher) access
- Access should be granted at either:
  - Subscription level, or
  - Resource Group level

Without proper IAM permissions, pushing images or creating resources will fail.

---

## Azure Container Registry (ACR)

### Create Azure Container Registry

1. Create a new **Azure Container Registry**
2. Enable **Managed Identity**
3. While configuring the registry:
   - Provide **only the registry name**
   - Specify the **image tag**
4. Ensure the registry is successfully created before proceeding

---

## Local Prerequisites

- **Docker Desktop must be running** on your local machine
- Azure CLI must be installed and authenticated

---

## Build and Push Docker Image

### Build Docker Image Locally

Run the following command from the project root where the `Dockerfile` is present:

```bash
docker build -t learning:1.0 .
```

Login to Azure Container Registry
```
az acr login --name crlearningdev
```

Tag the Image
```
docker tag learning:1.0 crlearningdev.azurecr.io/learning:1.0
```

Push the Image to ACR
```
docker push crlearningdev.azurecr.io/learning:1.0
```

Azure App Service (Container)
- Create Web App
- Use Linux Container
- Select the image from Azure Container Registry
- Enable System Assigned Managed Identity
- Azure Key Vault Configuration
- Enable Managed Identity Access
- Go to Key Vault → Access policies / IAM
- Grant the Web App access to Get Secrets
- Configure Application Settings

In Web App → Configuration → Application settings, add environment variables:
Example for Key Vault secret reference:

```
@Microsoft.KeyVault(SecretUri=https://learningjava-kv.vault.azure.net/secrets/sql-db-password/)
```
Mandatory Setting
```
WEBSITES_PORT=8080
```

This is required for container-based Spring Boot applications.

## CI/CD: Automatic Deployment Using GitHub Actions
Whenever code is committed, the application should:

- Build
- Create a Docker image
- Push it to ACR
- Deploy automatically to Azure App Service
- Create Service Principal for GitHub Actions.
For this go to Microsoft Entra Id -> App Registration -> Register an app (This creates a service principal)
Get all the important values 
1. ClientId (id of the registered app)
2. TenantId
3. SubscriptionId
   
- No need of Client Secret because we will using OIDC 
- This Service Principal allows GitHub Actions to authenticate and deploy to Azure.

### GitHub Secrets Configuration

Add secrets only in GitHub Repository → Settings → Secrets and variables → Actions:
Add secrets in the repository secrtes not in the environment secrets.

Recommended secrets:
- AZURE_CLIENT_ID
- AZURE_TENANT_ID
- AZURE_SUBSCRIPTION_ID

## Federated Credentials Setup

- Go to Microsoft Entra ID → App Registration
- Open your GitHub Actions app
- Navigate to Certificates & secrets → Federated credentials
Add details:Organization, Repository, Entity type (Branch / Environment / Pull Request)

This enables passwordless authentication from GitHub Actions.

## GitHub Actions Workflow

- Create the file: .github/workflows/deploy.yml
- Paste your deployment workflow that:
- Logs into Azure
- Builds the Docker image
- Pushes to ACR
- Deploys to Azure App Service
---
