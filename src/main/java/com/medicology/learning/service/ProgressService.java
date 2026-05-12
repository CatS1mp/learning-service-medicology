package com.medicology.learning.service;

import com.medicology.learning.dto.response.ContentActivityResponse;
import com.medicology.learning.dto.response.ContentActivitySummaryResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseStatus;
import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.repository.UserCourseRepository;
import com.medicology.learning.repository.UserDailyStreakRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Tiến độ học chỉ trả về danh sách enrollment. Phần completion/activity
 * do FE tổng hợp từ assessment-service (qua các API attempt) để tránh
 * BE-to-BE communication.
 */
@Service
@RequiredArgsConstructor
public class ProgressService {
    private final UserCourseRepository userCourseRepository;
    private final UserDailyStreakRepository userDailyStreakRepository;

    public List<CourseProgressResponse> getUserProgress(UUID userId) {
        List<UserCourse> enrolled = userCourseRepository.findByUserIdAndStatusOrderByEnrolledAtDesc(userId, UserCourseStatus.ENROLLED);
        return enrolled.stream()
                .map(uc -> CourseProgressResponse.builder()
                        .courseId(uc.getCourse().getId())
                        .courseName(uc.getCourse().getName())
                        .courseSlug(uc.getCourse().getSlug())
                        .lastStudiedAt(null)
                        .completionPercent(0)
                        .build())
                .collect(Collectors.toList());
    }

    public ContentActivitySummaryResponse getContentActivity(UUID userId, int days) {
        int normalizedDays = Math.max(1, days);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(normalizedDays - 1L);

        List<ContentActivityResponse> activity = new ArrayList<>();
        for (int offset = 0; offset < normalizedDays; offset++) {
            LocalDate currentDate = startDate.plusDays(offset);
            activity.add(ContentActivityResponse.builder()
                    .date(currentDate)
                    .completedContents(0)
                    .build());
        }

        return ContentActivitySummaryResponse.builder()
                .totalCompletedContents(0)
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
}
