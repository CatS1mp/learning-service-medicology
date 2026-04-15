package com.medicology.learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.learning.dto.request.LessonContentBlockRequest;
import com.medicology.learning.entity.LessonContentBlockKind;
import com.medicology.learning.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class LessonContentBlockValidator {
    private final ObjectMapper objectMapper;

    public void validate(List<LessonContentBlockRequest> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        Set<Integer> orderIndexes = new HashSet<>();
        for (int index = 0; index < blocks.size(); index++) {
            LessonContentBlockRequest block = blocks.get(index);
            String path = "blocks[" + index + "]";
            validateBasicFields(path, block, orderIndexes);
            JsonNode payloadNode = parsePayload(path, block.getPayload());
            validatePayloadByKind(path, block.getKind(), payloadNode);
            validateAssessmentReference(path, block);
        }
    }

    private void validateBasicFields(String path, LessonContentBlockRequest block, Set<Integer> orderIndexes) {
        if (block == null) {
            throw new InvalidRequestException(path + " must not be null.");
        }
        if (block.getOrderIndex() == null) {
            throw new InvalidRequestException(path + ".orderIndex is required.");
        }
        if (!orderIndexes.add(block.getOrderIndex())) {
            throw new InvalidRequestException("Duplicate block orderIndex: " + block.getOrderIndex());
        }
        if (block.getKind() == null) {
            throw new InvalidRequestException(path + ".kind is required.");
        }
        if (block.getPayload() == null || block.getPayload().isBlank()) {
            throw new InvalidRequestException(path + ".payload is required.");
        }
    }

    private JsonNode parsePayload(String path, String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new InvalidRequestException(path + ".payload must be valid JSON.", ex);
        }
    }

    private void validatePayloadByKind(String path, LessonContentBlockKind kind, JsonNode payloadNode) {
        switch (kind) {
            case RICH_TEXT -> requireFields(path, payloadNode, "title", "body");
            case INFOGRAPHIC -> validateInfographicPayload(path, payloadNode);
            case QUIZ_MCQ -> {
                requireFields(path, payloadNode, "question", "options", "correctOptionIndex");
                requireArrayMin(path, payloadNode, "options", 2);
            }
            case FILL_IN_THE_BLANKS -> {
                requireFields(path, payloadNode, "template", "answers");
                requireArrayMin(path, payloadNode, "answers", 1);
            }
            case SHORT_ANSWER -> requireFields(path, payloadNode, "prompt", "sampleAnswer");
            case FLASHCARD -> requireFields(path, payloadNode, "front", "back");
            case MATCHING -> {
                requireFields(path, payloadNode, "prompt", "pairs");
                requireArrayMin(path, payloadNode, "pairs", 1);
            }
            case ORDERING -> {
                requireFields(path, payloadNode, "prompt", "items");
                requireArrayMin(path, payloadNode, "items", 2);
            }
            case HOTSPOT_IMAGE -> {
                requireFields(path, payloadNode, "imageUrl", "hotspots");
                requireArrayMin(path, payloadNode, "hotspots", 1);
            }
            case TIMELINE -> {
                requireFields(path, payloadNode, "title", "events");
                requireArrayMin(path, payloadNode, "events", 1);
            }
            default -> throw new InvalidRequestException(path + ".kind is not supported.");
        }
    }

    private void validateAssessmentReference(String path, LessonContentBlockRequest block) {
        boolean gradableKind = EnumSet.of(
                LessonContentBlockKind.QUIZ_MCQ,
                LessonContentBlockKind.FILL_IN_THE_BLANKS,
                LessonContentBlockKind.SHORT_ANSWER
        ).contains(block.getKind());

        if (block.getQuestionId() != null && block.getAssessmentId() == null) {
            throw new InvalidRequestException(path + ".assessmentId is required when questionId is set.");
        }

        if (gradableKind && (block.getAssessmentId() == null || block.getQuestionId() == null)) {
            throw new InvalidRequestException(path + " gradable blocks require both assessmentId and questionId.");
        }

        if (!gradableKind && (block.getAssessmentId() != null || block.getQuestionId() != null)) {
            throw new InvalidRequestException(path + " assessment references are only allowed for gradable kinds.");
        }
    }

    private void requireFields(String path, JsonNode payloadNode, String... fields) {
        for (String field : fields) {
            JsonNode value = payloadNode.get(field);
            if (value == null || value.isNull() || (value.isTextual() && value.asText().isBlank())) {
                throw new InvalidRequestException(path + ".payload." + field + " is required for this kind.");
            }
        }
    }

    private void requireArrayMin(String path, JsonNode payloadNode, String field, int minSize) {
        JsonNode value = payloadNode.get(field);
        if (value == null || !value.isArray() || value.size() < minSize) {
            throw new InvalidRequestException(path + ".payload." + field + " must contain at least " + minSize + " item(s).");
        }
    }

    private void validateInfographicPayload(String path, JsonNode payloadNode) {
        requireFields(path, payloadNode, "title");

        String mediaType = readOptionalText(payloadNode, "mediaType");
        if (mediaType == null) {
            if (hasNonBlankText(payloadNode, "imageUrl") || hasNonBlankText(payloadNode, "videoUrl")) {
                return;
            }
            throw new InvalidRequestException(path + ".payload.mediaType is required when both imageUrl and videoUrl are missing.");
        }

        switch (mediaType) {
            case "image" -> requireFields(path, payloadNode, "imageUrl");
            case "video" -> requireFields(path, payloadNode, "videoUrl");
            default -> throw new InvalidRequestException(path + ".payload.mediaType must be either 'image' or 'video'.");
        }
    }

    private String readOptionalText(JsonNode payloadNode, String field) {
        JsonNode value = payloadNode.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private boolean hasNonBlankText(JsonNode payloadNode, String field) {
        return readOptionalText(payloadNode, field) != null;
    }
}
