package com.infinityisland.dao.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document("users")
public class User {
    @Id
    private String id;

    private String name;
    private String pin;

    /** First-time selectable, then locked unless explicitly reset. */
    private String theme;

    /** Node keeps per-level progress in a Map keyed like "L1", "L2", ... */
    private Map<String, ProgressState> progress = new HashMap<>();

    /** Node stores per-day stats keyed by YYYY-MM-DD. */
    private Map<String, DailyStats> dailyStats = new HashMap<>();

    /** New: keep login streak in days (Pacific time). */
    private Integer currentStreak = 0;

    /** New: last login date in YYYY-MM-DD (Pacific time). */
    private String lastLoginDate;

    public User() {}

    // --- Getters / Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public Map<String, ProgressState> getProgress() { return progress; }
    public void setProgress(Map<String, ProgressState> progress) { this.progress = progress; }

    public Map<String, DailyStats> getDailyStats() { return dailyStats; }
    public void setDailyStats(Map<String, DailyStats> dailyStats) { this.dailyStats = dailyStats; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public String getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }
}
