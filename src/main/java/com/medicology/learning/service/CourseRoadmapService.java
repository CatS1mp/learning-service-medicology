package com.medicology.learning.service;

import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot.InProgressAttemptItem;
import com.medicology.learning.dto.response.CourseRoadmapResponse;
import com.medicology.learning.dto.response.CourseRoadmapResponse.RoadmapContinueLessonResponse;
import com.medicology.learning.dto.response.CourseRoadmapResponse.RoadmapLessonNodeResponse;
import com.medicology.learning.dto.response.CourseRoadmapResponse.RoadmapProgressResponse;
import com.medicology.learning.dto.response.CourseRoadmapResponse.RoadmapSectionResponse;
import com.medicology.learning.entity.Content;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.Section;
import com.medicology.learning.repository.ContentRepository;
import com.medicology.learning.repository.CourseRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseRoadmapService {

    private final CourseRepository courseRepository;
    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public CourseRoadmapResponse buildLearnerRoadmap(String slugOrId, AssessmentUserProgressSnapshot snapshot) {
        Course course = resolveCourseForRoadmap(slugOrId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học: " + slugOrId));

        List<Content> flattenedLessons = flattenLessons(course);
        List<UUID> orderedContentIds = flattenedLessons.stream().map(Content::getId).toList();
        Set<UUID> passedIds = LearningProgressCalculator.passedContentIds(snapshot);
        Map<UUID, Boolean> outcomePassed = LearningProgressCalculator.latestOutcomePassedByContent(snapshot);
        Map<UUID, UUID> inProgressByContent = inProgressByContent(snapshot);

        int lessonOrder = 0;
        List<RoadmapSectionResponse> sections = new ArrayList<>();
        if (course.getSections() != null) {
            List<Section> orderedSections = course.getSections().stream()
                    .sorted(Comparator.comparing(Section::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                    .toList();
            for (Section section : orderedSections) {
                List<Content> contents = section.getContents() == null
                        ? List.of()
                        : section.getContents().stream()
                                .sorted(Comparator.comparing(Content::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                                .toList();
                List<RoadmapLessonNodeResponse> nodes = new ArrayList<>();
                for (Content lesson : contents) {
                    int lessonIndex = orderedContentIds.indexOf(lesson.getId());
                    String status = resolveLessonStatus(
                            lesson.getId(), lessonIndex, orderedContentIds, passedIds, outcomePassed, inProgressByContent);
                    nodes.add(RoadmapLessonNodeResponse.builder()
                            .id(lesson.getId())
                            .slug(lesson.getSlug())
                            .title(lesson.getName())
                            .status(status)
                            .type("lesson")
                            .orderIndex(++lessonOrder)
                            .inProgressAttemptId(inProgressByContent.get(lesson.getId()))
                            .description(describeLesson(lesson))
                            .build());
                }
                sections.add(RoadmapSectionResponse.builder()
                        .id(section.getId())
                        .title(section.getName())
                        .nodes(nodes)
                        .build());
            }
        }

        RoadmapContinueLessonResponse continueLesson = resolveContinueLesson(course, flattenedLessons, passedIds, inProgressByContent);

        int totalLessons = flattenedLessons.size();
        int completedLessons = LearningProgressCalculator.passedCountInCourse(orderedContentIds, passedIds);
        return CourseRoadmapResponse.builder()
                .topicTitle(course.getName())
                .courseSlug(course.getSlug())
                .courseImageUrl(course.getIconFileName())
                .progress(RoadmapProgressResponse.builder()
                        .current(completedLessons)
                        .total(totalLessons)
                        .build())
                .sections(sections)
                .continueLesson(continueLesson)
                .build();
    }

    private static String resolveLessonStatus(
            UUID contentId,
            int lessonIndex,
            List<UUID> orderedContentIds,
            Set<UUID> passedIds,
            Map<UUID, Boolean> outcomePassed,
            Map<UUID, UUID> inProgressByContent) {
        if (passedIds.contains(contentId)) {
            return "completed";
        }
        if (inProgressByContent.containsKey(contentId)) {
            return "active";
        }
        Boolean passed = outcomePassed.get(contentId);
        if (passed != null && !passed) {
            return "failed";
        }
        if (isLessonUnlocked(lessonIndex, orderedContentIds, passedIds)) {
            return "active";
        }
        return "locked";
    }

    private static boolean isLessonUnlocked(int lessonIndex, List<UUID> orderedContentIds, Set<UUID> passedIds) {
        if (lessonIndex <= 0) {
            return true;
        }
        for (int i = 0; i < lessonIndex; i++) {
            if (!passedIds.contains(orderedContentIds.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static RoadmapContinueLessonResponse resolveContinueLesson(
            Course course,
            List<Content> flattenedLessons,
            Set<UUID> passedIds,
            Map<UUID, UUID> inProgressByContent) {
        Content inProgress = flattenedLessons.stream()
                .filter(content -> inProgressByContent.containsKey(content.getId()))
                .findFirst()
                .orElse(null);
        Content next = flattenedLessons.stream()
                .filter(content -> !passedIds.contains(content.getId()))
                .findFirst()
                .orElse(null);
        Content target = inProgress != null ? inProgress : next;
        if (target == null) {
            return null;
        }
        return RoadmapContinueLessonResponse.builder()
                .courseInfo(course.getName())
                .title(target.getName())
                .description(
                        target.getDescription() != null
                                ? target.getDescription()
                                : (inProgress != null ? "Tiếp tục bài học đang làm dở." : "Tiếp tục bài học tiếp theo."))
                .contentSlug(target.getSlug())
                .inProgressAttemptId(inProgressByContent.get(target.getId()))
                .build();
    }

    private static List<Content> flattenLessons(Course course) {
        List<Content> flattened = new ArrayList<>();
        if (course.getSections() == null) {
            return flattened;
        }
        course.getSections().stream()
                .sorted(Comparator.comparing(Section::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                .forEach(section -> {
                    if (section.getContents() == null) {
                        return;
                    }
                    section.getContents().stream()
                            .sorted(Comparator.comparing(Content::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                            .forEach(flattened::add);
                });
        return flattened;
    }

    private static Map<UUID, UUID> inProgressByContent(AssessmentUserProgressSnapshot snapshot) {
        Map<UUID, UUID> map = new HashMap<>();
        if (snapshot == null || snapshot.getInProgressAttempts() == null) {
            return map;
        }
        for (InProgressAttemptItem item : snapshot.getInProgressAttempts()) {
            if (item != null && item.getContentId() != null && item.getAttemptId() != null) {
                map.put(item.getContentId(), item.getAttemptId());
            }
        }
        return map;
    }

    private static String describeLesson(Content lesson) {
        if (lesson.getEstimatedDurationMinutes() != null) {
            return lesson.getEstimatedDurationMinutes() + " phút học";
        }
        return lesson.getDifficultyLevel();
    }

    private Optional<Course> resolveCourseForRoadmap(String slugOrId) {
        if (slugOrId == null || slugOrId.isBlank()) {
            return Optional.empty();
        }
        String trimmed = slugOrId.trim();
        Optional<Course> course = courseRepository.findBySlugWithSections(trimmed);
        if (course.isEmpty()) {
            try {
                UUID courseId = UUID.fromString(trimmed);
                course = courseRepository.findByIdWithSections(courseId);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        }
        course.ifPresent(this::hydrateSectionContents);
        return course;
    }

    private void hydrateSectionContents(Course course) {
        if (course.getSections() == null || course.getSections().isEmpty()) {
            return;
        }
        List<UUID> sectionIds =
                course.getSections().stream().map(Section::getId).toList();
        if (sectionIds.isEmpty()) {
            return;
        }
        Map<UUID, List<Content>> contentsBySection =
                contentRepository.findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                        .collect(Collectors.groupingBy(content -> content.getSection().getId()));
        for (Section section : course.getSections()) {
            section.setContents(contentsBySection.getOrDefault(section.getId(), List.of()));
        }
    }
}
