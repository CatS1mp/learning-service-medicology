package com.medicology.learning.repository;

import com.medicology.learning.entity.SectionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SectionTestRepository extends JpaRepository<SectionTest, UUID> {
}
