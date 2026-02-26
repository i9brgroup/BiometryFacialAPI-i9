package com.i9brgroup.jbarreto.facial_auth_i9.web.sdk.aws;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class S3Service {

    private final S3Credentials s3Credentials;
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    private final Tika tika = new Tika();

    public S3Service(S3Credentials s3Credentials) {
        this.s3Credentials = s3Credentials;
    }

    private final Cache<String, String> urlCache = Caffeine.newBuilder()
            .expireAfterWrite(55, TimeUnit.MINUTES)
            .removalListener((key, value, cause) ->
                    log.debug("Cache REMOVIDO para key: {} | Motivo: {} ", key, cause)
                    )
            .maximumSize(1000)
            .build();

    public AwsBasicCredentials credentials() {
        System.out.println("Get a credentials an s3");
        return AwsBasicCredentials.create(s3Credentials.getAccessKey(),
                s3Credentials.getSecretKey());
    }

    public S3Client s3Client() {
        System.out.println("Connecting a provider bucket s3");
        Region region = Region.of(s3Credentials.getRegion());
        System.out.println("Using AWS region from config: " + region.id());
        return S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials()))
                .build();
    }

    public String getPreSignedUrl(String keyName) {
        String cacheUrl = urlCache.getIfPresent(keyName);

        if (cacheUrl != null) {
            log.info("[CACHE HIT] URL Recuperada da memoria para: {} ", keyName);
            return cacheUrl;
        }

        log.info("[CACHE MISS] Gerando nova URL no S3 para: {}", keyName);
        String newUrl = generatedPreSignedUrlForPhotosEmployees(keyName);
        urlCache.put(keyName, newUrl);
        log.info("[CACHE ADDED] URL adicionada na memoria para: {}", keyName);
        return newUrl;
    }

    public boolean uploadFile(MultipartFile multipartFile, String keyName) {
        String contentType = multipartFile.getContentType();
        try {
            if (contentType == null ||
                    !(contentType.equals("image/jpeg") ||
                            contentType.equals("image/png") ||
                            contentType.equals("image/jpg")) || !isValidImageFormat(multipartFile)) {
                log.error("Tipo de arquivo inválido: {}", contentType);
                throw new IllegalArgumentException("Apenas arquivos JPEG e PNG são permitidos.");
            }
        }catch (IOException ioException){
            log.error("Erro ao validar o formato do arquivo: {}", ioException.getMessage());
            throw new RuntimeException("Falha ao validar o formato do arquivo", ioException);
        }

        try {
            var response = s3Client().putObject(PutObjectRequest.builder()
                            .bucket(s3Credentials.getBucketName())
                            .key("photos/" + keyName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            
            return response.sdkHttpResponse().isSuccessful();
        } catch (IOException e) {
            log.error("Erro ao obter InputStream do arquivo: {}", e.getMessage());
            throw new RuntimeException("Falha ao processar o arquivo para upload", e);
        } catch (S3Exception e) {
            log.error("Erro ao fazer upload para o S3. AWS Error Code: {} Message: {}", 
                    e.awsErrorDetails().errorCode(), e.getMessage());
            throw e;
        }
    }

    public String generatedPreSignedUrlForPhotosEmployees(String keyName) {
        Region region = Region.of(s3Credentials.getRegion());
        System.out.println("Creating presigned URL using AWS region: " + region.id());

        // Ensure we generate the presigned URL for the same object key used on upload
        String objectKey = keyName;
        if (!objectKey.startsWith("photos/")) {
            objectKey = "photos/" + objectKey;
        }
        System.out.println("Generating presigned URL for object key: " + objectKey);

        try (S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials()))
                .build()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(s3Credentials.getBucketName())
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(55))  // The URL will expire in 55 minutes.
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            System.out.println("Presigned URL: " + presignedRequest.url().toString());
            System.out.println("HTTP method: " + presignedRequest.httpRequest().method());

            return presignedRequest.url().toExternalForm();
        }
    }

    public boolean deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Credentials.getBucketName())
                    .key("photos/" + key)
                    .build();

            var response = s3Client().deleteObject(deleteObjectRequest);
            if (response.sdkHttpResponse().isSuccessful()) {
                log.info("Rollback realizado: Arquivo {} removido do S3.", key);
            }
            return response.sdkHttpResponse().isSuccessful();

        }catch (S3Exception e) {
            log.error("FALHA CRÍTICA NO ROLLBACK: Não foi possível deletar o arquivo {}. Erro: {}", key, e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    public boolean isValidImageFormat(MultipartFile file) throws IOException {
        String detectedType = tika.detect(file.getInputStream());

        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/jpg");
        return allowedTypes.contains(detectedType);
    }
}
