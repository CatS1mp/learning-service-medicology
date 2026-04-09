package com.medicology.learning.service;

import com.medicology.learning.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private SupabaseStorageService supabaseStorageService;

    @BeforeEach
    void setUp() {
        supabaseStorageService = new SupabaseStorageService(
                restTemplate,
                "https://project.supabase.co/",
                "Course Image",
                "test-secret-key");
    }

    @Test
    void uploadCourseIconUsesConfiguredBucketAndReturnsPublicUrl() throws IOException {
        MockMultipartFile iconFile = new MockMultipartFile(
                "iconFile",
                "heart icon.png",
                "image/png",
                createPngBytes(400, 300));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("uploaded"));

        String publicUrl = supabaseStorageService.uploadCourseIcon(iconFile);

        ArgumentCaptor<String> uploadUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(uploadUrlCaptor.capture(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        assertThat(uploadUrlCaptor.getValue())
                .startsWith("https://project.supabase.co/storage/v1/object/course-image/course-icons/");
        assertThat(publicUrl)
                .startsWith("https://project.supabase.co/storage/v1/object/public/course-image/course-icons/");
        assertThat(publicUrl).endsWith("-heart-icon.png");
    }

    @Test
    void uploadCourseIconRejectsMissingFile() {
        MockMultipartFile iconFile = new MockMultipartFile(
                "iconFile",
                "heart.png",
                "image/png",
                new byte[0]);

        assertThatThrownBy(() -> supabaseStorageService.uploadCourseIcon(iconFile))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Course icon file is required");

        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void uploadCourseIconRejectsImageWithUnexpectedDimensions() throws IOException {
        MockMultipartFile iconFile = new MockMultipartFile(
                "iconFile",
                "heart.png",
                "image/png",
                createPngBytes(200, 200));

        assertThatThrownBy(() -> supabaseStorageService.uploadCourseIcon(iconFile))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Course icon must use a 4:3 aspect ratio, for example 256x192 pixels");

        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    private byte[] createPngBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
