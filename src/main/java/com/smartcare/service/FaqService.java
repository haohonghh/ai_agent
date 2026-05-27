package com.smartcare.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.smartcare.model.entity.FaqKnowledge;
import com.smartcare.repository.FaqKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqKnowledgeRepository faqKnowledgeRepository;
    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index.faq:smartcare_faq}")
    private String faqIndex;

    public Page<FaqKnowledge> searchByKeyword(String keyword, Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return faqKnowledgeRepository.searchByKeywords(keyword, pageable);
        }
        return faqKnowledgeRepository.findByStatus("active", pageable);
    }

    public List<Map<String, Object>> semanticSearch(String query, int topK) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            Query matchQuery = MatchQuery.of(m -> m
                    .field("question")
                    .query(query)
            )._toQuery();

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(faqIndex)
                    .query(matchQuery)
                    .size(topK)
            );

            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

            for (var hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", source.get("id"));
                    item.put("question", source.get("question"));
                    item.put("answer", source.get("answer"));
                    item.put("score", hit.score());
                    results.add(item);
                }
            }
        } catch (Exception e) {
            log.error("Elasticsearch semantic search failed", e);
        }
        return results;
    }

    public FaqKnowledge getFaqById(Long id) {
        return faqKnowledgeRepository.findById(id).orElse(null);
    }

    public boolean incrementViewCount(Long id) {
        return faqKnowledgeRepository.findById(id).map(faq -> {
            faq.setViewCount(faq.getViewCount() + 1);
            faqKnowledgeRepository.save(faq);
            return true;
        }).orElse(false);
    }

    public boolean incrementHelpfulCount(Long id) {
        return faqKnowledgeRepository.findById(id).map(faq -> {
            faq.setHelpfulCount(faq.getHelpfulCount() + 1);
            faqKnowledgeRepository.save(faq);
            return true;
        }).orElse(false);
    }
}