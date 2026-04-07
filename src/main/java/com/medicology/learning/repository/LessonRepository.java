package com.medicology.learning.repository;

import com.medicology.learning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findBySectionIdOrderByOrderIndexAsc(UUID sectionId);

    @Query("select count(l) from Lesson l where l.section.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);
}
