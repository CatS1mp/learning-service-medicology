package com.medicology.learning.service;

import com.medicology.learning.dto.response.ContentActivitySummaryResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseStatus;
import com.medicology.learning.repository.UserCourseRepository;
import com.medicology.learning.repository.UserDailyStreakRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private UserCourseRepository userCourseRepository;

    @Mock
    private UserDailyStreakRepository userDailyStreakRepository;

    @InjectMocks
    private ProgressService progressService;

    @Test
    void getUserProgressReturnsEnrollmentsOnly() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111001");
        UUID courseOneId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
        UUID courseTwoId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2");

        Course courseOne = buildCourse(courseOneId, "Tim mach co ban", "tim-mach-co-ban");
        Course courseTwo = buildCourse(courseTwoId, "Ho hap co ban", "ho-hap-co-ban");

        List<UserCourse> enrolled = List.of(
                UserCourse.builder().userId(userId).courseId(courseOneId).course(courseOne).status(UserCourseStatus.ENROLLED).build(),
                UserCourse.builder().userId(userId).courseId(courseTwoId).course(courseTwo).status(UserCourseStatus.ENROLLED).build());

        when(userCourseRepository.findByUserIdAndStatusOrderByEnrolledAtDesc(userId, UserCourseStatus.ENROLLED))
                .thenReturn(enrolled);

        List<CourseProgressResponse> result = progressService.getUserProgress(userId);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(item -> {
            assertThat(item.getCompletionPercent()).isZero();
            assertThat(item.getLastStudiedAt()).isNull();
        });
        assertThat(result.get(0).getCourseId()).isEqualTo(courseOneId);
        assertThat(result.get(1).getCourseId()).isEqualTo(courseTwoId);
    }

    @Test
    void getContentActivityReturnsEmptySeriesAcrossRequestedDays() {
        UUID userId = UUID.randomUUID();

        ContentActivitySummaryResponse summary = progressService.getContentActivity(userId, 7);

        assertThat(summary.getActivities()).hasSize(7);
        assertThat(summary.getTotalCompletedContents()).isZero();
        assertThat(summary.getActivities()).allSatisfy(item -> assertThat(item.getCompletedContents()).isZero());
    }

    private Course buildCourse(UUID courseId, String name, String slug) {
        return Course.builder()
                .id(courseId)
                .name(name)
                .slug(slug)
                .description("Description")
                .build();
    }
}
