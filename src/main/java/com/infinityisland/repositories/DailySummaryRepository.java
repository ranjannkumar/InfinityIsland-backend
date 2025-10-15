package com.infinityisland.repositories;

import com.infinityisland.dao.DailySummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DailySummaryRepository extends MongoRepository<DailySummary, String> {
    Optional<DailySummary> findByUserIdAndDate(String userId, String date);
    Optional<DailySummary> findByDateAndReportSentMarker(String date, boolean reportSentMarker);

    List<DailySummary> findByUserId(String userId);
}
