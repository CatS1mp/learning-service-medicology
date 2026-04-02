package com.medicology.learning.service;

import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.request.CourseStatusRequest;
import com.medicology.learning.dto.response.CourseResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.Section;
import com.medicology.learning.entity.Theme;
import com.medicology.learning.repository.CourseRepository;
import com.medicology.learning.repository.SectionRepository;
import com.medicology.learning.repository.ThemeRepository;
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

    public CourseResponse createCourse(CourseRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + request.getSectionId()));
        Course course = Course.builder()
                .section(section)
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .orderIndex(request.getOrderIndex())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .difficultyLevel(request.getDifficultyLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .content(request.getContent())
                .build();
        return mapToResponse(courseRepository.save(course));
    }

    public CourseResponse updateCourse(UUID id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + request.getSectionId()));
        course.setSection(section);
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setSlug(request.getSlug());
        course.setOrderIndex(request.getOrderIndex());
        course.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        course.setDifficultyLevel(request.getDifficultyLevel());
        course.setIsActive(request.getIsActive());
        course.setContent(request.getContent());
        return mapToResponse(courseRepository.save(course));
    }

    public CourseResponse updateCourseStatus(UUID id, CourseStatusRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));
        course.setIsActive(request.getIsActive());
        return mapToResponse(courseRepository.save(course));
    }

    public void deleteCourse(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found with ID: " + id);
        }
        courseRepository.deleteById(id);
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .sectionId(course.getSection().getId())
                .name(course.getName())
                .description(course.getDescription())
                .slug(course.getSlug())
                .orderIndex(course.getOrderIndex())
                .estimatedDurationMinutes(course.getEstimatedDurationMinutes())
                .difficultyLevel(course.getDifficultyLevel())
                .isActive(course.getIsActive())
                .content(course.getContent())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
