package com.infinityisland.repositories;

import com.infinityisland.dao.QuestionTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionTemplateRepository extends MongoRepository<QuestionTemplate, String> {
    List<QuestionTemplate> findByOperationAndLevelAndBelt(String operation, String level, String belt);
    long countByOperationAndLevelAndBelt(String operation, String level, String belt);
}
