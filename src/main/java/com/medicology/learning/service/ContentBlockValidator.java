package com.medicology.learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.learning.dto.request.ContentBlockRequest;
import com.medicology.learning.entity.ContentBlockKind;
import com.medicology.learning.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ContentBlockValidator {
    private final ObjectMapper objectMapper;

    public void validate(List<ContentBlockRequest> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        Set<Integer> orderIndexes = new HashSet<>();
        for (int index = 0; index < blocks.size(); index++) {
            ContentBlockRequest block = blocks.get(index);
            String path = "blocks[" + index + "]";
            validateBasicFields(path, block, orderIndexes);
            JsonNode payloadNode = parsePayload(path, block.getPayload());
            validatePayloadByKind(path, block.getKind(), payloadNode);
            validateGradableFields(path, block);
        }
    }

    private void validateBasicFields(String path, ContentBlockRequest block, Set<Integer> orderIndexes) {
        if (block == null) {
            throw new InvalidRequestException(path + " không được để trống.");
        }
        if (block.getOrderIndex() == null) {
            throw new InvalidRequestException(path + ".orderIndex là bắt buộc.");
        }
        if (!orderIndexes.add(block.getOrderIndex())) {
            throw new InvalidRequestException("Trùng orderIndex khối: " + block.getOrderIndex());
        }
        if (block.getKind() == null) {
            throw new InvalidRequestException(path + ".kind là bắt buộc.");
        }
        if (block.getPayload() == null || block.getPayload().isBlank()) {
            throw new InvalidRequestException(path + ".payload là bắt buộc.");
        }
    }

    private JsonNode parsePayload(String path, String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new InvalidRequestException(path + ".payload phải là JSON hợp lệ.", ex);
        }
    }

    private void validatePayloadByKind(String path, ContentBlockKind kind, JsonNode payloadNode) {
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
            case TIMELINE -> {
                requireFields(path, payloadNode, "title", "events");
                requireArrayMin(path, payloadNode, "events", 1);
            }
            default -> throw new InvalidRequestException(path + ".kind không được hỗ trợ.");
        }
    }

    private void validateGradableFields(String path, ContentBlockRequest block) {
        if (Boolean.TRUE.equals(block.getIsGradable())) {
            if (block.getMaxScore() == null || block.getMaxScore() < 1) {
                throw new InvalidRequestException(path + ".maxScore phải ≥ 1 khi isGradable=true.");
            }
        } else {
            if (block.getMaxScore() != null) {
                throw new InvalidRequestException(path + ".maxScore phải null khi isGradable=false hoặc bỏ qua.");
            }
        }
    }

    private void requireFields(String path, JsonNode payloadNode, String... fields) {
        for (String field : fields) {
            JsonNode value = payloadNode.get(field);
            if (value == null || value.isNull() || (value.isTextual() && value.asText().isBlank())) {
                throw new InvalidRequestException(path + ".payload." + field + " là bắt buộc cho loại khối này.");
            }
        }
    }

    private void requireArrayMin(String path, JsonNode payloadNode, String field, int minSize) {
        JsonNode value = payloadNode.get(field);
        if (value == null || !value.isArray() || value.size() < minSize) {
            throw new InvalidRequestException(
                    path + ".payload." + field + " phải có ít nhất " + minSize + " phần tử.");
        }
    }

    private void validateInfographicPayload(String path, JsonNode payloadNode) {
        requireFields(path, payloadNode, "title");

        String mediaType = readOptionalText(payloadNode, "mediaType");
        if (mediaType == null) {
            if (hasNonBlankText(payloadNode, "imageUrl") || hasNonBlankText(payloadNode, "videoUrl")) {
                return;
            }
            throw new InvalidRequestException(
                    path + ".payload.mediaType là bắt buộc khi thiếu cả imageUrl và videoUrl.");
        }

        switch (mediaType) {
            case "image" -> requireFields(path, payloadNode, "imageUrl");
            case "video" -> requireFields(path, payloadNode, "videoUrl");
            default -> throw new InvalidRequestException(path + ".payload.mediaType phải là 'image' hoặc 'video'.");
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
