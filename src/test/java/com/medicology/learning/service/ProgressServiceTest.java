package com.medicology.learning.service;

import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.Lesson;
import com.medicology.learning.entity.Section;
import com.medicology.learning.entity.UserLesson;
import com.medicology.learning.repository.LessonRepository;
import com.medicology.learning.repository.UserDailyStreakRepository;
import com.medicology.learning.repository.UserLessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private UserLessonRepository userLessonRepository;

    @Mock
    private UserDailyStreakRepository userDailyStreakRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private ProgressService progressService;

    @Test
    void getUserProgressReturnsOnlyIncompleteCoursesSortedByLatestStudyDate() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111001");
        UUID courseOneId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
        UUID courseTwoId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2");

        Course courseOne = buildCourse(courseOneId, "Tim mach co ban", "tim-mach-co-ban");
        Course courseTwo = buildCourse(courseTwoId, "Ho hap co ban", "ho-hap-co-ban");

        List<UserLesson> userLessons = List.of(
                buildUserLesson(userId, UUID.fromString("10000000-0000-0000-0000-000000000001"), courseOne,
                        LocalDateTime.of(2026, 4, 7, 21, 0)),
                buildUserLesson(userId, UUID.fromString("10000000-0000-0000-0000-000000000002"), courseOne,
                        LocalDateTime.of(2026, 4, 6, 20, 0)),
                buildUserLesson(userId, UUID.fromString("20000000-0000-0000-0000-000000000001"), courseTwo,
                        LocalDateTime.of(2026, 4, 5, 19, 0))
        );

        when(userLessonRepository.findByUserId(userId)).thenReturn(userLessons);
        when(lessonRepository.countByCourseId(courseOneId)).thenReturn(4L);
        when(lessonRepository.countByCourseId(courseTwoId)).thenReturn(1L);

        List<CourseProgressResponse> result = progressService.getUserProgress(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseId()).isEqualTo(courseOneId);
        assertThat(result.get(0).getCourseName()).isEqualTo("Tim mach co ban");
        assertThat(result.get(0).getLastStudiedAt()).isEqualTo(LocalDateTime.of(2026, 4, 7, 21, 0));
        assertThat(result.get(0).getCompletionPercent()).isEqualTo(50);
    }

    private UserLesson buildUserLesson(UUID userId, UUID lessonId, Course course, LocalDateTime completedAt) {
        Section section = Section.builder()
                .id(UUID.randomUUID())
                .course(course)
                .name("Section")
                .slug("section")
                .orderIndex(1)
                .estimatedDurationMinutes(20)
                .build();

        Lesson lesson = Lesson.builder()
                .id(lessonId)
                .section(section)
                .name("Lesson")
                .description("Description")
                .slug("lesson")
                .orderIndex(1)
                .estimatedDurationMinutes(10)
                .difficultyLevel("beginner")
                .isActive(true)
                .content("{}")
                .build();

        return UserLesson.builder()
                .userId(userId)
                .lessonId(lessonId)
                .lesson(lesson)
                .quizzesCorrect(3)
                .completedAt(completedAt)
                .build();
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
