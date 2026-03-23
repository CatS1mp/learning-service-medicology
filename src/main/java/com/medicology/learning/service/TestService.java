package com.medicology.learning.service;

import com.medicology.learning.entity.SectionTest;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseId;
import com.medicology.learning.entity.UserSectionTest;
import com.medicology.learning.entity.UserSectionTestId;
import com.medicology.learning.repository.SectionTestRepository;
import com.medicology.learning.repository.UserCourseRepository;
import com.medicology.learning.repository.UserSectionTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TestService {
    private final SectionTestRepository sectionTestRepository;
    private final UserSectionTestRepository userSectionTestRepository;
    private final UserCourseRepository userCourseRepository;

    public SectionTest getSectionTestContent(UUID sectionId) {
        return sectionTestRepository.findById(sectionId)
            .orElseThrow(() -> new IllegalArgumentException("Section test not found"));
    }

    public void submitCourseQuiz(UUID userId, UUID courseId, int quizzesCorrect) {
        UserCourseId id = new UserCourseId(userId, courseId);
        UserCourse userCourse = userCourseRepository.findById(id)
            .orElseGet(() -> UserCourse.builder().userId(userId).courseId(courseId).quizzesCorrect(0).build());
        
        userCourse.setQuizzesCorrect(Math.max(userCourse.getQuizzesCorrect(), quizzesCorrect));
        userCourseRepository.save(userCourse);
    }

    public void submitSectionTest(UUID userId, UUID sectionId, int quizzesCorrect, int totalQuestions) {
        SectionTest test = sectionTestRepository.findById(sectionId)
            .orElseThrow(() -> new IllegalArgumentException("Section test not found"));
            
        double percentage = (double) quizzesCorrect / totalQuestions * 100;
        boolean passed = percentage >= test.getPassingScorePercentage().doubleValue();

        UserSectionTestId id = new UserSectionTestId(userId, sectionId);
        UserSectionTest result = UserSectionTest.builder()
            .userId(userId)
            .sectionTestId(sectionId)
            .quizzesCorrect(quizzesCorrect)
            .totalQuestions(totalQuestions)
            .passed(passed)
            .build();
            
        userSectionTestRepository.save(result);
    }
    
    public List<UserSectionTest> getUserTestResults(UUID userId) {
        return userSectionTestRepository.findByUserId(userId);
    }
}
