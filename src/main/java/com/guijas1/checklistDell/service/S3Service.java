package com.guijas1.checklistDell.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    public String uploadImagem(MultipartFile file) throws IOException {
        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo inválido.");
        }

        // Remove acentos e caracteres especiais, troca espaços e vírgulas por "_"
        String originalName = file.getOriginalFilename();
        String nomeLimpo = Normalizer.normalize(originalName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")       // remove acentos
                .replaceAll("[^a-zA-Z0-9\\.\\-_]", "_"); // remove símbolos exceto ponto, traço, underline

        String nomeUnico = UUID.randomUUID() + "_" + nomeLimpo;

        try (S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nomeUnico)
                    .build();

            s3.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        }

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + nomeUnico;
    }
}
