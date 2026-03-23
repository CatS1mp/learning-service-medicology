package com.medicology.learning.repository;

import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, UserCourseId> {
    List<UserCourse> findByUserId(UUID userId);
}
