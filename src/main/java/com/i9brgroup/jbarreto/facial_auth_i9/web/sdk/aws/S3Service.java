package com.i9brgroup.jbarreto.facial_auth_i9.web.sdk.aws;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class S3Service {

    private final S3Credentials s3Credentials;

    public S3Service(S3Credentials s3Credentials) {
        this.s3Credentials = s3Credentials;
    }

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

    public boolean uploadFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        try {
            s3Client().putObject(PutObjectRequest.builder()
                            .bucket(s3Credentials.getBucketName())
                            .key("photos/" + originalFilename)
                            .contentType("image/jpg")
                            .build(),
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

            System.out.println("File " + originalFilename + " uploaded successfully.");
            return true;
        } catch (IOException e) {
            System.out.println("Error getting InputStream from file: " + e.getMessage());
            throw new RuntimeException("Failed to get InputStream from file", e);
        } catch (S3Exception e) {
            System.out.println("Error uploading file to S3. AWS Error Code: " + e.awsErrorDetails().errorCode() + " Message: {}" +
                    e.getMessage());
            throw e;
        }
    }
}
