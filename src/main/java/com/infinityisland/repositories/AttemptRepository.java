package com.infinityisland.repositories;

import com.infinityisland.dao.Attempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends MongoRepository<Attempt, String> {
    List<Attempt> findByQuizRunIdOrderByIdAsc(String quizRunId);
    Optional<Attempt> findByQuizRunIdAndQuestionId(String quizRunId, String questionId);
    long countByQuizRunId(String quizRunId);
    long countByQuizRunIdAndCorrectTrue(String quizRunId);
}
