package com.medicology.learning.repository;

import com.medicology.learning.entity.ContentBlockProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentBlockProgressRepository extends JpaRepository<ContentBlockProgress, UUID> {
    Optional<ContentBlockProgress> findByAttemptIdAndContentBlockId(UUID attemptId, UUID contentBlockId);
}
