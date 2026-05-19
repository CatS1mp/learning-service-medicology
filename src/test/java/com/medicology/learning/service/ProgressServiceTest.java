package com.medicology.learning.service;

import com.medicology.learning.client.AssessmentProgressClient;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot.ContentOutcomeItem;
import com.medicology.learning.dto.response.ContentActivitySummaryResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseStatus;
import com.medicology.learning.repository.ContentRepository;
import com.medicology.learning.repository.UserCourseRepository;
import com.medicology.learning.repository.UserDailyStreakRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private AssessmentProgressClient assessmentProgressClient;

    @InjectMocks
    private ProgressService progressService;

    @Test
    void getUserProgressUsesPassedCompletionsFromAssessmentSnapshot() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111001");
        UUID courseOneId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
        UUID contentOne = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
        UUID contentTwo = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        Course courseOne = buildCourse(courseOneId, "Tim mach co ban", "tim-mach-co-ban");
        List<UserCourse> enrolled = List.of(UserCourse.builder()
                .userId(userId)
                .courseId(courseOneId)
                .course(courseOne)
                .status(UserCourseStatus.ENROLLED)
                .build());

        AssessmentUserProgressSnapshot snapshot = new AssessmentUserProgressSnapshot();
        ContentOutcomeItem passed = new ContentOutcomeItem();
        passed.setContentId(contentOne);
        passed.setPassed(true);
        passed.setCompletedAt(Instant.parse("2026-05-19T10:00:00Z"));
        snapshot.setLatestFinalizedByContent(List.of(passed));
        snapshot.setInProgressAttempts(List.of());
        snapshot.setRecentGradedAttempts(List.of());

        when(userCourseRepository.findByUserIdAndStatusOrderByEnrolledAtDesc(userId, UserCourseStatus.ENROLLED))
                .thenReturn(enrolled);
        when(assessmentProgressClient.fetchUserProgressSnapshot(userId)).thenReturn(Optional.of(snapshot));
        when(contentRepository.findIdsByCourseId(courseOneId)).thenReturn(List.of(contentOne, contentTwo));

        List<CourseProgressResponse> result = progressService.getUserProgress(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompletionPercent()).isEqualTo(50);
        assertThat(result.get(0).getCompletedContentCount()).isEqualTo(1);
        assertThat(result.get(0).getTotalContentCount()).isEqualTo(2);
        assertThat(result.get(0).getLastStudiedAt()).isNotNull();
    }

    @Test
    void getContentActivityCountsPassedCompletionsByDay() {
        UUID userId = UUID.randomUUID();
        AssessmentUserProgressSnapshot snapshot = new AssessmentUserProgressSnapshot();
        ContentOutcomeItem passed = new ContentOutcomeItem();
        passed.setContentId(UUID.randomUUID());
        passed.setPassed(true);
        passed.setCompletedAt(Instant.now());
        snapshot.setLatestFinalizedByContent(List.of(passed));

        when(assessmentProgressClient.fetchUserProgressSnapshot(userId)).thenReturn(Optional.of(snapshot));

        ContentActivitySummaryResponse summary = progressService.getContentActivity(userId, 7);

        assertThat(summary.getActivities()).hasSize(7);
        assertThat(summary.getTotalCompletedContents()).isEqualTo(1);
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
