package com.medicology.learning.repository;

import com.medicology.learning.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findBySlugIgnoreCase(String slug);

    /** Chỉ fetch sections — contents load riêng (tránh MultipleBagFetchException). */
    @Query(
            "SELECT DISTINCT c FROM Course c "
                    + "LEFT JOIN FETCH c.sections "
                    + "WHERE lower(c.slug) = lower(:slug)")
    Optional<Course> findBySlugWithSections(@Param("slug") String slug);

    @Query(
            "SELECT DISTINCT c FROM Course c "
                    + "LEFT JOIN FETCH c.sections "
                    + "WHERE c.id = :courseId")
    Optional<Course> findByIdWithSections(@Param("courseId") UUID courseId);
}
