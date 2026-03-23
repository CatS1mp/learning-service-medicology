package com.medicology.learning.service;

import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.Section;
import com.medicology.learning.entity.Theme;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseId;
import com.medicology.learning.repository.CourseRepository;
import com.medicology.learning.repository.SectionRepository;
import com.medicology.learning.repository.ThemeRepository;
import com.medicology.learning.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final ThemeRepository themeRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }

    public List<Section> getSectionsByTheme(UUID themeId) {
        return sectionRepository.findByThemeIdOrderByOrderIndexAsc(themeId);
    }

    public List<Course> getCoursesBySection(UUID sectionId) {
        return courseRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);
    }

    public Map<String, Object> getLearningPath() {
        Map<String, Object> path = new HashMap<>();
        path.put("themes", themeRepository.findAll());
        return path;
    }

    public Course getCourseContent(UUID courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    public void enrollCourse(UUID userId, UUID courseId) {
        UserCourseId id = new UserCourseId(userId, courseId);
        if (!userCourseRepository.existsById(id)) {
            UserCourse enrollment = UserCourse.builder()
                .userId(userId)
                .courseId(courseId)
                .quizzesCorrect(0)
                .build();
            userCourseRepository.save(enrollment);
        }
    }
}
