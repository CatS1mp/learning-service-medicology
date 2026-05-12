package com.medicology.learning.repository;

import com.medicology.learning.entity.ContentBlockTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentBlockTemplateRepository extends JpaRepository<ContentBlockTemplate, UUID> {
    List<ContentBlockTemplate> findByIsActiveTrueOrderByKindAscNameAsc();
}
