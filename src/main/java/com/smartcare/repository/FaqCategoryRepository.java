package com.smartcare.repository;

import com.smartcare.model.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaqCategoryRepository extends JpaRepository<FaqCategory, Long> {

    List<FaqCategory> findByParentIdIsNull();

    List<FaqCategory> findByParentId(Long parentId);

    List<FaqCategory> findByStatusOrderBySortOrderAsc(String status);
}