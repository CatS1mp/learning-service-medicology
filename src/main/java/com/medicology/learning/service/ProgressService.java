package com.medicology.learning.service;

import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.entity.UserLesson;
import com.medicology.learning.repository.UserDailyStreakRepository;
import com.medicology.learning.repository.UserLessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final UserLessonRepository userLessonRepository;
    private final UserDailyStreakRepository userDailyStreakRepository;

    public List<UserLesson> getUserProgress(UUID userId) {
        return userLessonRepository.findByUserId(userId);
    }

    public UserDailyStreak updateStreak(UUID userId) {
        UserDailyStreak streak = userDailyStreakRepository.findById(userId)
            .orElseGet(() -> {
                UserDailyStreak newStreak = new UserDailyStreak();
                newStreak.setUserId(userId);
                newStreak.setCurrentStreak(0);
                newStreak.setLongestStreak(0);
                newStreak.setTotalActiveDays(0);
                return newStreak;
            });

        LocalDate today = LocalDate.now();
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

        return userDailyStreakRepository.save(streak);
    }
}
