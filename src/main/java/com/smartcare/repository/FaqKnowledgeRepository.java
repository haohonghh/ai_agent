package com.smartcare.repository;

import com.smartcare.model.entity.FaqKnowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaqKnowledgeRepository extends JpaRepository<FaqKnowledge, Long> {

    Page<FaqKnowledge> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<FaqKnowledge> findByStatus(String status, Pageable pageable);

    @Query(value = "SELECT * FROM faq_knowledge WHERE status = 'active' AND keywords && CAST(:keyword AS text[])", nativeQuery = true)
    Page<FaqKnowledge> searchByKeywords(@Param("keyword") String keyword, Pageable pageable);

    List<FaqKnowledge> findByViewCountGreaterThanEqualOrderByViewCountDesc(int minCount);
}