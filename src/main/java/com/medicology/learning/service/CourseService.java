package com.medicology.learning.service;

import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.response.CourseResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseStatus;
import com.medicology.learning.repository.CourseRepository;
import com.medicology.learning.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final SectionService sectionService;

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
    }

    public CourseResponse getCourseRoadmap(UUID courseId) {
        return getCourseById(courseId);
    }

    public CourseResponse createCourse(CourseRequest request) {
        Course course = Course.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .iconFileName(request.getIconFileName())
                .colorCode(request.getColorCode())
                .orderIndex(request.getOrderIndex())
                .build();
        return mapToResponse(courseRepository.save(course));
    }

    public CourseResponse updateCourse(UUID courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
        course.setName(request.getName());
        course.setSlug(request.getSlug());
        course.setDescription(request.getDescription());
        course.setIconFileName(request.getIconFileName());
        course.setColorCode(request.getColorCode());
        course.setOrderIndex(request.getOrderIndex());
        return mapToResponse(courseRepository.save(course));
    }

    public void deleteCourse(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }

    public Map<String, Object> getLearningPath() {
        Map<String, Object> path = new HashMap<>();
        path.put("courses", getAllCourses());
        return path;
    }

    public List<CourseResponse> getEnrolledCourses(UUID userId) {
        return userCourseRepository.findByUserIdAndStatusOrderByEnrolledAtDesc(userId, UserCourseStatus.ENROLLED)
                .stream()
                .map(UserCourse::getCourse)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse enrollCourse(UUID userId, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        UserCourse userCourse = userCourseRepository.findByUserIdAndCourseId(userId, courseId)
                .map(existing -> {
                    existing.setStatus(UserCourseStatus.ENROLLED);
                    return existing;
                })
                .orElseGet(() -> UserCourse.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .status(UserCourseStatus.ENROLLED)
                        .build());

        userCourseRepository.save(userCourse);
        return mapToResponse(course);
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .slug(course.getSlug())
                .description(course.getDescription())
                .iconFileName(course.getIconFileName())
                .colorCode(course.getColorCode())
                .orderIndex(course.getOrderIndex())
                .sections(course.getSections() != null ? course.getSections().stream()
                        .map(sectionService::mapToSummaryResponse)
                        .collect(Collectors.toList()) : null)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
