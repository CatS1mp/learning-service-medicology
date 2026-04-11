package com.medicology.learning.repository;

import com.medicology.learning.entity.LessonContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonContentBlockRepository extends JpaRepository<LessonContentBlock, UUID> {
    List<LessonContentBlock> findByLessonIdOrderByOrderIndexAsc(UUID lessonId);
}
