package com.medicology.learning.repository;

import com.medicology.learning.entity.AiLearningFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface AiLearningFeedbackRepository extends JpaRepository<AiLearningFeedback, UUID> {
    List<AiLearningFeedback> findByUserId(UUID userId);
}
