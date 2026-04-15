package com.medicology.learning.service;

import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.dto.response.LessonActivityResponse;
import com.medicology.learning.dto.response.LessonActivitySummaryResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.entity.UserLesson;
import com.medicology.learning.repository.LessonRepository;
import com.medicology.learning.repository.UserDailyStreakRepository;
import com.medicology.learning.repository.UserLessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final UserLessonRepository userLessonRepository;
    private final UserDailyStreakRepository userDailyStreakRepository;
    private final LessonRepository lessonRepository;

    public List<CourseProgressResponse> getUserProgress(UUID userId) {
        List<UserLesson> userLessons = userLessonRepository.findByUserId(userId);

        Map<UUID, List<UserLesson>> progressByCourseId = userLessons.stream()
                .collect(Collectors.groupingBy(userLesson -> userLesson.getLesson().getSection().getCourse().getId()));

        return progressByCourseId.values().stream()
                .map(this::mapToCourseProgress)
                .sorted(Comparator.comparing(CourseProgressResponse::getLastStudiedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public LessonActivitySummaryResponse getLessonActivity(UUID userId, int days) {
        int normalizedDays = Math.max(1, days);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(normalizedDays - 1L);

        Map<LocalDate, Long> completedLessonsByDate = userLessonRepository.findByUserId(userId).stream()
                .map(UserLesson::getCompletedAt)
                .filter(completedAt -> completedAt != null)
                .map(LocalDateTime::toLocalDate)
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(today))
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        List<LessonActivityResponse> activity = new ArrayList<>();
        for (int offset = 0; offset < normalizedDays; offset++) {
            LocalDate currentDate = startDate.plusDays(offset);
            activity.add(LessonActivityResponse.builder()
                    .date(currentDate)
                    .completedLessons(completedLessonsByDate.getOrDefault(currentDate, 0L).intValue())
                    .build());
        }

        int totalCompletedLessons = activity.stream()
                .mapToInt(LessonActivityResponse::getCompletedLessons)
                .sum();

        return LessonActivitySummaryResponse.builder()
                .totalCompletedLessons(totalCompletedLessons)
                .activities(activity)
                .build();
    }

    public UserDailyStreak updateStreak(UUID userId) {
        LocalDate today = LocalDate.now();
        UserDailyStreak streak = userDailyStreakRepository.findById(userId)
                .orElseGet(() -> createInitialStreak(userId));
        applyStreakRules(streak, today);

        try {
            return userDailyStreakRepository.save(streak);
        } catch (DataIntegrityViolationException ex) {
            // Concurrent first-time pings can race on insert with same user_id.
            UserDailyStreak existing = userDailyStreakRepository.findById(userId)
                    .orElseThrow(() -> ex);
            applyStreakRules(existing, today);
            return userDailyStreakRepository.save(existing);
        }
    }

    private UserDailyStreak createInitialStreak(UUID userId) {
        UserDailyStreak newStreak = new UserDailyStreak();
        newStreak.setUserId(userId);
        newStreak.setCurrentStreak(0);
        newStreak.setLongestStreak(0);
        newStreak.setTotalActiveDays(0);
        return newStreak;
    }

    private void applyStreakRules(UserDailyStreak streak, LocalDate today) {
        if (streak.getLastActivityDate() == null) {
            streak.setCurrentStreak(1);
            streak.setLongestStreak(1);
            streak.setTotalActiveDays(1);
            streak.setStreakStartedAt(today);
            streak.setLastActivityDate(today);
        } else if (streak.getLastActivityDate().isEqual(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setLongestStreak(Math.max(streak.getLongestStreak(), streak.getCurrentStreak()));
            streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
            streak.setLastActivityDate(today);
        } else if (streak.getLastActivityDate().isBefore(today.minusDays(1))) {
            streak.setCurrentStreak(1);
            streak.setStreakStartedAt(today);
            streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
            streak.setLastActivityDate(today);
        }
    }

    private CourseProgressResponse mapToCourseProgress(List<UserLesson> userLessons) {
        UserLesson latestLesson = userLessons.stream()
                .max(Comparator.comparing(UserLesson::getCompletedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new IllegalArgumentException("No lesson progress found"));

        Course course = latestLesson.getLesson().getSection().getCourse();
        long totalLessons = lessonRepository.countByCourseId(course.getId());
        int completionPercent = totalLessons == 0
                ? 0
                : (int) Math.min(100, Math.round((userLessons.size() * 100.0) / totalLessons));

        return CourseProgressResponse.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .courseSlug(course.getSlug())
                .lastStudiedAt(latestCompletedAt(userLessons))
                .completionPercent(completionPercent)
                .build();
    }

    private LocalDateTime latestCompletedAt(List<UserLesson> userLessons) {
        return userLessons.stream()
                .map(UserLesson::getCompletedAt)
                .filter(completedAt -> completedAt != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
