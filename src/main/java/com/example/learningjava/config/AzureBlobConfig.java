package com.example.learningjava.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Bean
    public DefaultAzureCredential defaultAzureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    // Suppose you want to store different types of files in different containers, it is possible
    // to create multiple BlobContainerClient beans here with different container names.
    // Then inject the specific BlobContainerClient where needed using @Qualifier annotation.
    
    @Bean
    public BlobServiceClient blobServiceClient(
            DefaultAzureCredential credential,
            @Value("${azure.storage.account-name}") String accountName) {

        String endpoint = "https://" + accountName + ".blob.core.windows.net";

        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(
            BlobServiceClient blobServiceClient,
            @Value("${azure.storage.container-name}") String containerName) {

        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
