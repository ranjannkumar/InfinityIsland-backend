package com.infinityisland.dao.resource;

import com.infinityisland.dao.user.DailyStats;
import com.infinityisland.dao.user.ProgressState;

import java.util.Map;

/**
 * Node-shaped authenticated user payload:
 * {
 *   user: {
 *     id, name, theme,
 *     grandTotal,
 *     progress: { "L1": {...}, ... },
 *     dailyStats: { correctCount, totalActiveMs },
 *     currentStreak,
 *     lastLoginDate
 *   },
 *   token: "..."
 * }
 */
public class AuthUser {
    private String id;
    private String name;
    private String theme;
    private Map<String, ProgressState> progress;
    private DailyStats dailyStats; // today's summary (date can be null)
    private Long grandTotal;
    private Integer currentStreak;
    private String lastLoginDate; // YYYY-MM-DD (Pacific)

    public AuthUser() {
        this.grandTotal = 0L;
        this.currentStreak = 0;
    }

    public AuthUser(String id,
                    String name,
                    String theme,
                    Map<String, ProgressState> progress,
                    DailyStats dailyStats,
                    Long grandTotal,
                    Integer currentStreak,
                    String lastLoginDate) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.progress = progress;
        this.dailyStats = dailyStats;
        this.grandTotal = grandTotal;
        this.currentStreak = currentStreak;
        this.lastLoginDate = lastLoginDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Map<String, ProgressState> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, ProgressState> progress) {
        this.progress = progress;
    }

    public DailyStats getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(DailyStats dailyStats) {
        this.dailyStats = dailyStats;
    }

    public Long getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Long grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}