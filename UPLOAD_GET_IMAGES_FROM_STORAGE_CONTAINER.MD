# Upload and get images to/from the storage container.

First to connect to the azure storage container we need to create a bean. This is available in Azure blob Config.

### AzureBlobConfig 
This class creates and wires Azure Blob clients once, using Managed Identity, so your whole application can safely and efficiently talk to Azure Blob Storage.

### AzureBlobService
It handles image uploads and deletions in Azure Blob Storage using a preconfigured BlobContainerClient. It validates uploaded files (ensuring they are non-empty images under 5 MB), generates a secure unique blob name, uploads the file to Azure, and sets the correct content type so the image renders properly when accessed.

### How in locally it works ?
When you run the application locally, Azure authentication is handled transparently by the Azure SDK using DefaultAzureCredential. Since Managed Identity is only available in Azure, the SDK automatically falls back to your local Azure identity, usually from an active az login session or your IDE’s Azure sign-in. When your service uploads or reads a blob, the SDK requests a short-lived OAuth access token from Azure Active Directory on your behalf, attaches it to the request, and Azure Blob Storage validates that token and checks whether your user account has the required RBAC role (such as Storage Blob Data Contributor) on the storage account.

### generateReadSasUrl - ProfileImageService

generateReadSasUrl creates a temporary, secure, read-only URL for a private image stored in Azure Blob Storage. Because the container is private, the blob cannot be accessed directly. The method first identifies the specific blob, then asks Azure (using the app’s identity) for a User Delegation Key, which is a short-lived signing key issued by Azure Active Directory. Using this key, the method cryptographically signs a Shared Access Signature (SAS) that allows only read access, only over HTTPS, and only for a short time window (for example, 15 minutes). The result is a URL that anyone can use to view the image during that time, but which automatically expires and cannot be reused or escalated to write or delete permissions.

#### Sas token
A SAS token (Shared Access Signature) is a temporary access pass to an Azure resource.

#### User Delegation Key
A User Delegation Key is a temporary signing key issued by Azure Active Directory.

A User Delegation Key is used to cryptographically sign a SAS token.

After signing, the signature is appended to the SAS token as part of the query string.

The delegation key controls how long your app can create SAS tokens, while the SAS token controls how long the blob can be accessed by the client.