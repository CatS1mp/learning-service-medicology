package com.medicology.learning.repository;

import com.medicology.learning.entity.UserCourse;
import com.medicology.learning.entity.UserCourseId;
import com.medicology.learning.entity.UserCourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, UserCourseId> {
    List<UserCourse> findByUserIdAndStatusOrderByEnrolledAtDesc(UUID userId, UserCourseStatus status);
    Optional<UserCourse> findByUserIdAndCourseId(UUID userId, UUID courseId);
}
