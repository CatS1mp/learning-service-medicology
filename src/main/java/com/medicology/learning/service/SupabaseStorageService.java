package com.medicology.learning.service;

import com.medicology.learning.exception.InvalidFileException;
import com.medicology.learning.exception.StorageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private static final String COURSE_ICON_PREFIX = "course-icons/";
    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String storageBucket;
    private final String serviceRoleKey;

    @Autowired
    public SupabaseStorageService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.storage.bucket}") String storageBucket,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {
        this(restTemplateBuilder.build(), supabaseUrl, storageBucket, serviceRoleKey);
    }

    SupabaseStorageService(
            RestTemplate restTemplate,
            String supabaseUrl,
            String storageBucket,
            String serviceRoleKey) {
        this.restTemplate = restTemplate;
        this.supabaseUrl = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        this.storageBucket = normalizeBucketName(storageBucket);
        this.serviceRoleKey = serviceRoleKey;
    }

    public String uploadCourseIcon(MultipartFile iconFile) {
        validateFile(iconFile);

        String objectPath = buildObjectPath(iconFile.getOriginalFilename());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resolveContentType(iconFile.getContentType()));
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.set("x-upsert", "false");

        try {
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(iconFile.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    buildUploadUrl(objectPath),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new StorageUploadException("Supabase did not accept the course icon upload", null);
            }

            return buildPublicUrl(objectPath);
        } catch (HttpStatusCodeException ex) {
            throw new StorageUploadException(
                    "Failed to upload course icon to Supabase bucket '" + storageBucket
                            + "': " + ex.getResponseBodyAsString(),
                    ex);
        } catch (IOException | RestClientException ex) {
            throw new StorageUploadException(
                    "Failed to upload course icon to Supabase bucket '" + storageBucket + "'",
                    ex);
        }
    }

    String buildPublicUrl(String objectPath) {
        return supabaseUrl + "/storage/v1/object/public/" + encodeBucketAndPath(objectPath);
    }

    private void validateFile(MultipartFile iconFile) {
        if (iconFile == null || iconFile.isEmpty()) {
            throw new InvalidFileException("Course icon file is required");
        }
    }

    private String buildUploadUrl(String objectPath) {
        return supabaseUrl + "/storage/v1/object/" + encodeBucketAndPath(objectPath);
    }

    private String encodeBucketAndPath(String objectPath) {
        return UriUtils.encodePath(storageBucket + "/" + objectPath, StandardCharsets.UTF_8);
    }

    private String normalizeBucketName(String configuredBucket) {
        String trimmedBucket = Optional.ofNullable(configuredBucket)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalArgumentException("supabase.storage.bucket must not be blank"));

        String normalizedBucket = trimmedBucket
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-");

        if (!normalizedBucket.matches("^[a-z0-9][a-z0-9._-]*$")) {
            throw new IllegalArgumentException(
                    "supabase.storage.bucket must be a Supabase bucket ID, for example 'course-image'");
        }

        if (!trimmedBucket.equals(normalizedBucket)) {
            log.warn(
                    "Normalized Supabase bucket name from '{}' to '{}'. Configure the bucket ID directly to avoid upload issues.",
                    trimmedBucket,
                    normalizedBucket);
        }

        return normalizedBucket;
    }

    private String buildObjectPath(String originalFilename) {
        String sanitizedFileName = sanitizeFileName(originalFilename);
        return COURSE_ICON_PREFIX + UUID.randomUUID() + "-" + sanitizedFileName;
    }

    private String sanitizeFileName(String originalFilename) {
        String candidate = Optional.ofNullable(originalFilename)
                .map(name -> name.replace("\\", "/"))
                .map(name -> name.substring(name.lastIndexOf('/') + 1))
                .filter(StringUtils::hasText)
                .orElse("icon");

        String sanitized = candidate
                .replaceAll("\\s+", "-")
                .replaceAll("[^A-Za-z0-9._-]", "");

        return StringUtils.hasText(sanitized) ? sanitized : "icon";
    }

    private MediaType resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
