package com.infinityisland.repositories;

import com.infinityisland.dao.QuizRun;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRunRepository extends MongoRepository<QuizRun, String> {
    List<QuizRun> findByUserIdAndStatus(String userId, String status);
    Optional<QuizRun> findTopByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    Optional<QuizRun> findByIdAndUserId(String id, String userId);
}
