package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.aws;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class S3Credentials {

    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucketName;

    public S3Credentials(
            @Value("${aws.accessKeyId}")
            String accessKey,
            @Value("${aws.secretKey}")
            String secretKey,
            @Value("${aws.region}")
            String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucketName = "i9-biometric-images";
    }
}
