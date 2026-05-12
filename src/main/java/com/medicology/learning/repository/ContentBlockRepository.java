package com.medicology.learning.repository;

import com.medicology.learning.entity.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {
    List<ContentBlock> findByContentIdOrderByOrderIndexAsc(UUID contentId);
}
