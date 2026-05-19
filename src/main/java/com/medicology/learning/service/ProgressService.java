package com.medicology.learning.service;

import com.medicology.learning.client.AssessmentProgressClient;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot;
import com.medicology.learning.dto.response.ContentActivityResponse;
import com.medicology.learning.dto.response.ContentActivitySummaryResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.dto.response.DashboardProgressResponse;
import com.medicology.learning.dto.response.DashboardProgressResponse.RecentGradedAttemptResponse;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseStatus;
import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.repository.ContentRepository;
import com.medicology.learning.repository.UserCourseRepository;
import com.medicology.learning.repository.UserDailyStreakRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final UserCourseRepository userCourseRepository;
    private final UserDailyStreakRepository userDailyStreakRepository;
    private final ContentRepository contentRepository;
    private final AssessmentProgressClient assessmentProgressClient;

    public List<CourseProgressResponse> getUserProgress(UUID userId) {
        AssessmentUserProgressSnapshot snapshot = loadSnapshot(userId);
        Set<UUID> passedContentIds = LearningProgressCalculator.passedContentIds(snapshot);

        List<UserCourse> enrolled =
                userCourseRepository.findByUserIdAndStatusOrderByEnrolledAtDesc(userId, UserCourseStatus.ENROLLED);
        return enrolled.stream()
                .map(uc -> mapCourseProgress(uc, passedContentIds, snapshot))
                .collect(Collectors.toList());
    }

    public DashboardProgressResponse getDashboardProgress(UUID userId, int activityDays) {
        AssessmentUserProgressSnapshot snapshot = loadSnapshot(userId);
        Set<UUID> passedContentIds = LearningProgressCalculator.passedContentIds(snapshot);
        List<UserCourse> enrolled =
                userCourseRepository.findByUserIdAndStatusOrderByEnrolledAtDesc(userId, UserCourseStatus.ENROLLED);
        List<CourseProgressResponse> courses = enrolled.stream()
                .map(uc -> mapCourseProgress(uc, passedContentIds, snapshot))
                .collect(Collectors.toList());
        ContentActivitySummaryResponse activity = getContentActivity(userId, activityDays, snapshot);
        List<RecentGradedAttemptResponse> recentGraded = mapRecentGraded(snapshot);
        double averageScore = LearningProgressCalculator.averageScoreOnTenScale(snapshot);
        return DashboardProgressResponse.builder()
                .courses(courses)
                .activity(activity)
                .recentGradedAttempts(recentGraded)
                .averageScoreOnTenScale(averageScore)
                .build();
    }

    public ContentActivitySummaryResponse getContentActivity(UUID userId, int days) {
        return getContentActivity(userId, days, loadSnapshot(userId));
    }

    private ContentActivitySummaryResponse getContentActivity(
            UUID userId, int days, AssessmentUserProgressSnapshot snapshot) {
        int normalizedDays = Math.max(1, days);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(normalizedDays - 1L);
        var countsByDay = LearningProgressCalculator.passedCompletionsByDay(snapshot, startDate, today);

        List<ContentActivityResponse> activity = new ArrayList<>();
        int totalCompleted = 0;
        for (int offset = 0; offset < normalizedDays; offset++) {
            LocalDate currentDate = startDate.plusDays(offset);
            int completed = countsByDay.getOrDefault(currentDate, 0);
            totalCompleted += completed;
            activity.add(ContentActivityResponse.builder()
                    .date(currentDate)
                    .completedContents(completed)
                    .build());
        }

        return ContentActivitySummaryResponse.builder()
                .totalCompletedContents(totalCompleted)
                .activities(activity)
                .build();
    }

    public UserDailyStreak updateStreak(UUID userId) {
        LocalDate today = LocalDate.now();
        UserDailyStreak streak = userDailyStreakRepository
                .findById(userId)
                .orElseGet(() -> createInitialStreak(userId));
        applyStreakRules(streak, today);

        try {
            return userDailyStreakRepository.save(streak);
        } catch (DataIntegrityViolationException ex) {
            UserDailyStreak existing =
                    userDailyStreakRepository.findById(userId).orElseThrow(() -> ex);
            applyStreakRules(existing, today);
            return userDailyStreakRepository.save(existing);
        }
    }

    public AssessmentUserProgressSnapshot loadSnapshot(UUID userId) {
        return assessmentProgressClient.fetchUserProgressSnapshot(userId).orElse(null);
    }

    private CourseProgressResponse mapCourseProgress(
            UserCourse uc, Set<UUID> passedContentIds, AssessmentUserProgressSnapshot snapshot) {
        UUID courseId = uc.getCourse().getId();
        List<UUID> contentIds = contentRepository.findIdsByCourseId(courseId);
        int total = contentIds.size();
        int completed = (int) contentIds.stream().filter(passedContentIds::contains).count();
        int percent = LearningProgressCalculator.completionPercent(contentIds, passedContentIds);
        LocalDateTime lastStudied = LearningProgressCalculator.lastStudiedAt(contentIds, snapshot);

        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseName(uc.getCourse().getName())
                .courseSlug(uc.getCourse().getSlug())
                .lastStudiedAt(lastStudied)
                .completionPercent(percent)
                .completedContentCount(completed)
                .totalContentCount(total)
                .build();
    }

    private List<RecentGradedAttemptResponse> mapRecentGraded(AssessmentUserProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getRecentGradedAttempts() == null) {
            return List.of();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        List<RecentGradedAttemptResponse> items = new ArrayList<>();
        int limit = 6;
        for (var attempt : snapshot.getRecentGradedAttempts()) {
            if (attempt == null || attempt.getSubmittedAt() == null) {
                continue;
            }
            if (attempt.getMaxScore() == null || attempt.getScore() == null || attempt.getMaxScore().signum() <= 0) {
                continue;
            }
            double onTen = attempt.getScore()
                    .divide(attempt.getMaxScore(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN)
                    .doubleValue();
            onTen = Math.max(0d, Math.min(10d, onTen));
            items.add(RecentGradedAttemptResponse.builder()
                    .submittedAt(formatInstant(attempt.getSubmittedAt(), formatter))
                    .scoreOnTenScale(onTen)
                    .build());
            if (items.size() >= limit) {
                break;
            }
        }
        return items;
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        return formatter.format(instant.atZone(ZoneId.systemDefault()));
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
