package com.medicology.learning.service;

import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot.ContentOutcomeItem;
import com.medicology.learning.dto.response.RecommendationContextItemResponse;
import com.medicology.learning.entity.Content;
import com.medicology.learning.repository.ContentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationContextService {

    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public List<RecommendationContextItemResponse> buildRecentContext(
            AssessmentUserProgressSnapshot snapshot, int limit) {
        if (snapshot == null || snapshot.getLatestFinalizedByContent() == null) {
            return List.of();
        }
        int normalizedLimit = Math.max(1, Math.min(limit, 20));

        List<ContentOutcomeItem> sorted = snapshot.getLatestFinalizedByContent().stream()
                .filter(item -> item != null && item.getContentId() != null && item.getCompletedAt() != null)
                .sorted(Comparator.comparing(ContentOutcomeItem::getCompletedAt).reversed())
                .limit(normalizedLimit)
                .toList();

        if (sorted.isEmpty()) {
            return List.of();
        }

        List<UUID> contentIds = sorted.stream().map(ContentOutcomeItem::getContentId).toList();
        Map<UUID, Content> contentById = contentRepository.findAllByIdInWithSectionAndCourse(contentIds).stream()
                .collect(Collectors.toMap(Content::getId, Function.identity(), (a, b) -> a));

        List<RecommendationContextItemResponse> items = new ArrayList<>();
        for (ContentOutcomeItem outcome : sorted) {
            Content content = contentById.get(outcome.getContentId());
            items.add(RecommendationContextItemResponse.builder()
                    .contentId(outcome.getContentId())
                    .contentName(content != null ? content.getName() : outcome.getContentId().toString())
                    .courseName(content != null && content.getSection() != null && content.getSection().getCourse() != null
                            ? content.getSection().getCourse().getName()
                            : null)
                    .sectionName(content != null && content.getSection() != null ? content.getSection().getName() : null)
                    .submittedAt(outcome.getCompletedAt())
                    .passed(outcome.isPassed())
                    .build());
        }
        return items;
    }
}
