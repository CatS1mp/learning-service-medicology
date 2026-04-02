package com.medicology.learning.repository;

import com.medicology.learning.entity.UserLesson;
import com.medicology.learning.entity.UserLessonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserLessonRepository extends JpaRepository<UserLesson, UserLessonId> {
    List<UserLesson> findByUserId(UUID userId);
}
