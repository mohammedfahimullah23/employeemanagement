package com.example.learningjava.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class ProfileImageService {

    private final BlobContainerClient containerClient;
    private final BlobServiceClient blobServiceClient;
    private volatile UserDelegationKey cachedKey;
    private volatile OffsetDateTime keyExpiry;

    public ProfileImageService(BlobContainerClient containerClient,
            BlobServiceClient blobServiceClient,
            @Value("${azure.storage.account-name}") String accountName) {
        this.containerClient = containerClient;
        this.blobServiceClient = blobServiceClient;
    }

    public synchronized String generateReadSasUrl(String blobName) {

        if (blobName == null || blobName.isBlank()) {
            throw new IllegalArgumentException("Profile image not set");
        }

        UserDelegationKey delegationKey = getDelegationKey();

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                now.plusMinutes(10),
                permission)
                .setStartTime(now.minusMinutes(1))
                .setProtocol(SasProtocol.HTTPS_ONLY);

        return blobClient.getBlobUrl() + "?" +
                blobClient.generateUserDelegationSas(sasValues, delegationKey);
    }
    // The delegation key controls how long your app can create SAS tokens,
    // while the SAS token controls how long the blob can be accessed by the client.
    private UserDelegationKey getDelegationKey() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (cachedKey == null || keyExpiry.isBefore(now.plusMinutes(2))) {

            cachedKey = blobServiceClient.getUserDelegationKey(
                    now.minusMinutes(5),
                    now.plusMinutes(15));

            keyExpiry = now.plusMinutes(15);
        }

        return cachedKey;
    }

}
