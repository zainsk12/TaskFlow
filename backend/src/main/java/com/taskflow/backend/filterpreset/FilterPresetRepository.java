package com.taskflow.backend.filterpreset;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterPresetRepository extends MongoRepository<FilterPreset, String> {

    List<FilterPreset> findByUserId(String userId, Sort sort);

    Optional<FilterPreset> findByIdAndUserId(String id, String userId);

    boolean existsByUserIdAndName(String userId, String name);
}