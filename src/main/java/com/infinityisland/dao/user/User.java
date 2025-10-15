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
    private String theme;

    private Map<String, ProgressState> progress;
    private Map<String, DailyStats> dailyStats;

    public User() {
        theme = "animals";
        progress = new HashMap<>();
        dailyStats = new HashMap<>();
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

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
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

    public Map<String, DailyStats> getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(Map<String, DailyStats> dailyStats) {
        this.dailyStats = dailyStats;
    }
}
