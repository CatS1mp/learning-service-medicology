package com.medicology.learning.repository;

import com.medicology.learning.entity.UserLesson;
import com.medicology.learning.entity.UserLessonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLessonRepository extends JpaRepository<UserLesson, UserLessonId> {
    List<UserLesson> findByUserId(UUID userId);
    Optional<UserLesson> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    @Query("SELECT CASE WHEN COUNT(ul) > 0 THEN true ELSE false END FROM UserLesson ul JOIN ul.lesson l "
            + "WHERE ul.userId = :userId AND l.section.id = :sectionId")
    boolean existsByUserIdAndLessonSectionId(@Param("userId") UUID userId, @Param("sectionId") UUID sectionId);
}
