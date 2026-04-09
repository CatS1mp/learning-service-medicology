package com.medicology.learning.service;

import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.response.CourseResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.exception.StorageUploadException;
import com.medicology.learning.repository.CourseRepository;
import com.medicology.learning.repository.UserCourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    @Mock
    private SectionService sectionService;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @InjectMocks
    private CourseService courseService;

    @Test
    void createCourseUploadsIconAndPersistsPublicUrl() {
        CourseRequest request = new CourseRequest();
        request.setName("Tim mach co ban");
        request.setSlug("tim-mach-co-ban");
        request.setDescription("Khoa hoc nhap mon tim mach");
        request.setColorCode("#EF4444");
        request.setOrderIndex(1);

        MockMultipartFile iconFile = new MockMultipartFile(
                "iconFile",
                "heart icon.png",
                "image/png",
                "png-data".getBytes());
        String iconUrl = "https://example.supabase.co/storage/v1/object/public/Course%20Image/course-icons/icon.png";

        when(supabaseStorageService.uploadCourseIcon(iconFile)).thenReturn(iconUrl);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            return course;
        });

        CourseResponse response = courseService.createCourse(request, iconFile);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());

        assertThat(courseCaptor.getValue().getIconFileName()).isEqualTo(iconUrl);
        assertThat(response.getIconFileName()).isEqualTo(iconUrl);
        assertThat(response.getName()).isEqualTo("Tim mach co ban");
    }

    @Test
    void createCourseDoesNotSaveWhenIconUploadFails() {
        CourseRequest request = new CourseRequest();
        request.setName("Tim mach co ban");
        request.setSlug("tim-mach-co-ban");
        request.setDescription("Khoa hoc nhap mon tim mach");
        request.setColorCode("#EF4444");
        request.setOrderIndex(1);

        MockMultipartFile iconFile = new MockMultipartFile(
                "iconFile",
                "heart.png",
                "image/png",
                "png-data".getBytes());

        when(supabaseStorageService.uploadCourseIcon(iconFile))
                .thenThrow(new StorageUploadException("upload failed", new RuntimeException("boom")));

        assertThatThrownBy(() -> courseService.createCourse(request, iconFile))
                .isInstanceOf(StorageUploadException.class);

        verify(courseRepository, never()).save(any(Course.class));
    }
}
