package com.taskflow.backend.task;

import com.taskflow.backend.common.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@code MongoTemplate}-based implementation of {@link TaskRepositoryCustom}.
 *
 * <p>Builds an {@code AND} of only the supplied filters (all anchored on
 * {@code userId}), counts the total, then fetches the requested page with its
 * sort applied.
 */
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Task> search(TaskSearchCriteria c, Pageable pageable) {
        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("userId").is(c.userId()));

        if (c.status() != null) {
            filters.add(Criteria.where("status").is(c.status()));
        }
        if (c.priority() != null) {
            filters.add(Criteria.where("priority").is(c.priority()));
        }
        if (c.categoryId() != null && !c.categoryId().isBlank()) {
            filters.add(Criteria.where("categoryId").is(c.categoryId()));
        }
        if (c.dueAfter() != null || c.dueBefore() != null) {
            Criteria due = Criteria.where("dueDate");
            if (c.dueAfter() != null) {
                due = due.gte(c.dueAfter());
            }
            if (c.dueBefore() != null) {
                due = due.lte(c.dueBefore());
            }
            filters.add(due);
        }
        if (Boolean.TRUE.equals(c.overdue())) {
            filters.add(Criteria.where("dueDate").lt(Instant.now()));
            filters.add(Criteria.where("status").ne(TaskStatus.DONE));
        }
        if (c.search() != null && !c.search().isBlank()) {
            String regex = Pattern.quote(c.search().trim());
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i")
            ));
        }

        Criteria criteria = new Criteria().andOperator(filters.toArray(new Criteria[0]));
        Query query = new Query(criteria);

        long total = mongoTemplate.count(query, Task.class);
        query.with(pageable);
        List<Task> tasks = mongoTemplate.find(query, Task.class);

        return PageableExecutionUtils.getPage(tasks, pageable, () -> total);
    }

    @Override
    public Map<String, Long> countGroupedByStatus(String userId) {
        return countGroupedBy(userId, "status");
    }

    @Override
    public Map<String, Long> countGroupedByPriority(String userId) {
        return countGroupedBy(userId, "priority");
    }

    /** {@code $match userId} then {@code $group} by the given field with a count. */
    private Map<String, Long> countGroupedBy(String userId, String field) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.group(field).count().as("count")
        );
        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, Task.class, Document.class);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Document doc : results.getMappedResults()) {
            Object key = doc.get("_id");
            if (key != null) {
                counts.put(key.toString(), ((Number) doc.get("count")).longValue());
            }
        }
        return counts;
    }
}
