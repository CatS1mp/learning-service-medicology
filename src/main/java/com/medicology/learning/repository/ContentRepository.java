package com.medicology.learning.repository;

import com.medicology.learning.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {
    List<Content> findBySectionIdOrderByOrderIndexAsc(UUID sectionId);

    @Query("select count(c) from Content c where c.section.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);

    @Query("select c.id from Content c where c.section.course.id = :courseId")
    List<UUID> findIdsByCourseId(@Param("courseId") UUID courseId);
}
