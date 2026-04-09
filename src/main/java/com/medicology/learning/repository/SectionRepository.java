package com.medicology.learning.repository;

import com.medicology.learning.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    @Query("select count(s) from Section s where s.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);
}
