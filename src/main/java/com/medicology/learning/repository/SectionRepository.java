package com.medicology.learning.repository;

import com.medicology.learning.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByThemeIdOrderByOrderIndexAsc(UUID themeId);
}
