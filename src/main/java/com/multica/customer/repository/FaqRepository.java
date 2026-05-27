package com.multica.customer.repository;

import com.multica.customer.entity.Faq;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FAQ ES仓储接口
 */
@Repository
public interface FaqRepository extends ElasticsearchRepository<Faq, String> {

    /**
     * 根据分类查找FAQ
     */
    List<Faq> findByCategory(String category);

    /**
     * 根据关键词查找FAQ
     */
    List<Faq> findByKeywordsContaining(String keyword);
}
