package com.medicology.learning.repository;

import com.medicology.learning.entity.UserLessonBlockProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLessonBlockProgressRepository extends JpaRepository<UserLessonBlockProgress, UUID> {
    Optional<UserLessonBlockProgress> findByUserIdAndLessonContentBlockId(UUID userId, UUID lessonContentBlockId);

    List<UserLessonBlockProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);
}
