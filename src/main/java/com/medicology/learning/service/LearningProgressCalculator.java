package com.medicology.learning.service;

import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot.ContentOutcomeItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LearningProgressCalculator {

    public static Set<UUID> passedContentIds(AssessmentUserProgressSnapshot snapshot) {
        Set<UUID> passed = new HashSet<>();
        if (snapshot == null || snapshot.getLatestFinalizedByContent() == null) {
            return passed;
        }
        for (ContentOutcomeItem item : snapshot.getLatestFinalizedByContent()) {
            if (item != null && item.isPassed() && item.getContentId() != null) {
                passed.add(item.getContentId());
            }
        }
        return passed;
    }

    public static Map<UUID, Boolean> latestOutcomePassedByContent(AssessmentUserProgressSnapshot snapshot) {
        Map<UUID, Boolean> map = new HashMap<>();
        if (snapshot == null || snapshot.getLatestFinalizedByContent() == null) {
            return map;
        }
        for (ContentOutcomeItem item : snapshot.getLatestFinalizedByContent()) {
            if (item != null && item.getContentId() != null) {
                map.put(item.getContentId(), item.isPassed());
            }
        }
        return map;
    }

    public static int passedCountInCourse(List<UUID> courseContentIds, Set<UUID> passedContentIds) {
        if (courseContentIds == null || courseContentIds.isEmpty() || passedContentIds == null) {
            return 0;
        }
        return (int) courseContentIds.stream().filter(passedContentIds::contains).count();
    }

    public static int completionPercent(List<UUID> courseContentIds, Set<UUID> passedContentIds) {
        if (courseContentIds == null || courseContentIds.isEmpty()) {
            return 0;
        }
        long passed = passedCountInCourse(courseContentIds, passedContentIds);
        return (int) Math.min(100, Math.round((passed * 100.0) / courseContentIds.size()));
    }

    public static LocalDateTime lastStudiedAt(List<UUID> courseContentIds, AssessmentUserProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getLatestFinalizedByContent() == null || courseContentIds == null) {
            return null;
        }
        Set<UUID> courseIds = new HashSet<>(courseContentIds);
        Instant latest = null;
        for (ContentOutcomeItem item : snapshot.getLatestFinalizedByContent()) {
            if (item == null || item.getContentId() == null || !courseIds.contains(item.getContentId())) {
                continue;
            }
            if (item.getCompletedAt() == null) {
                continue;
            }
            if (latest == null || item.getCompletedAt().isAfter(latest)) {
                latest = item.getCompletedAt();
            }
        }
        return latest == null ? null : LocalDateTime.ofInstant(latest, ZoneId.systemDefault());
    }

    public static Map<LocalDate, Integer> passedCompletionsByDay(
            AssessmentUserProgressSnapshot snapshot, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> counts = new HashMap<>();
        if (snapshot == null || snapshot.getLatestFinalizedByContent() == null) {
            return counts;
        }
        for (ContentOutcomeItem item : snapshot.getLatestFinalizedByContent()) {
            if (item == null || !item.isPassed() || item.getCompletedAt() == null) {
                continue;
            }
            LocalDate day = item.getCompletedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            if (day.isBefore(startDate) || day.isAfter(endDate)) {
                continue;
            }
            counts.merge(day, 1, Integer::sum);
        }
        return counts;
    }

    public static double averageScoreOnTenScale(AssessmentUserProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getRecentGradedAttempts() == null
                || snapshot.getRecentGradedAttempts().isEmpty()) {
            return 0d;
        }
        double sum = 0d;
        int count = 0;
        for (var item : snapshot.getRecentGradedAttempts()) {
            if (item == null || item.getMaxScore() == null || item.getScore() == null) {
                continue;
            }
            if (item.getMaxScore().signum() <= 0) {
                continue;
            }
            double onTen = item.getScore()
                    .divide(item.getMaxScore(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN)
                    .doubleValue();
            sum += Math.max(0d, Math.min(10d, onTen));
            count++;
        }
        return count == 0 ? 0d : sum / count;
    }
}
