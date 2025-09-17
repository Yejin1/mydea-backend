package com.mydea.mydea_backend.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BlobSasService {

    private final BlobServiceClient blobServiceClient;

    /**
     * DB에 저장된 정규 Blob URL(쿼리 없는 URL)에 대해 읽기 전용 SAS URL을 생성
     * @param canonicalBlobUrl 예) https://mydea.blob.core.windows.net/works/1/123/preview_v1737060000.png
     * @param ttl 유효기간 (예: Duration.ofHours(1))
     */
    public String issueReadSasUrl(String canonicalBlobUrl, Duration ttl) {
        Parsed p = parseBlobUrl(canonicalBlobUrl);
        BlobClient blob = blobServiceClient
                .getBlobContainerClient(p.container)
                .getBlobClient(p.blobName);

        BlobSasPermission perm = new BlobSasPermission().setReadPermission(true);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(now.plus(ttl), perm)
                .setProtocol(SasProtocol.HTTPS_ONLY)
                .setStartTime(now.minusMinutes(1)); // 시계 오차 보정

        String sas = blob.generateSas(values);
        return blob.getBlobUrl() + "?" + sas;
    }

    private Parsed parseBlobUrl(String url) {
        // https://{account}.blob.core.windows.net/{container}/{blobPath...}
        URI u = URI.create(url);
        String path = u.getPath();
        if (path == null || path.length() < 2) {
            throw new IllegalArgumentException("Invalid blob URL: " + url);
        }
        String noSlash = path.startsWith("/") ? path.substring(1) : path;
        int firstSlash = noSlash.indexOf('/');
        if (firstSlash < 0) {
            throw new IllegalArgumentException("Blob URL must include container and blob path: " + url);
        }
        String container = noSlash.substring(0, firstSlash);
        String blobName  = noSlash.substring(firstSlash + 1);
        return new Parsed(container, blobName);
    }

    private record Parsed(String container, String blobName) {}
}
