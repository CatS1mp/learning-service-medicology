package com.medicology.learning.repository;

import com.medicology.learning.entity.UserSectionTest;
import com.medicology.learning.entity.UserSectionTestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserSectionTestRepository extends JpaRepository<UserSectionTest, UserSectionTestId> {
    List<UserSectionTest> findByUserId(UUID userId);
}
