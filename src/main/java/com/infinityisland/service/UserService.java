package com.infinityisland.service;

import com.infinityisland.dao.DailySummary;
import com.infinityisland.dao.user.*;
import com.infinityisland.repositories.DailySummaryRepository;
import com.infinityisland.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DailySummaryRepository dailySummaryRepository;

    public UserService(UserRepository userRepository,
                       DailySummaryRepository dailySummaryRepository) {
        this.userRepository = userRepository;
        this.dailySummaryRepository = dailySummaryRepository;
    }

    // ---- used by /auth/login-pin; kept here for completeness ----
    public User loginOrCreateByPin(String pin, String name) {
        if (pin == null || pin.isBlank()) throw new IllegalArgumentException("pin is required");
        String normalizedPin = pin.trim();
        String normalizedName = (name != null && !name.isBlank()) ? name.trim() : "Player";

        return userRepository.findByPin(normalizedPin).map(u -> {
            // Node: set name on first login only (adjust if your Node differs)
            if (u.getName() == null || u.getName().isBlank()) {
                u.setName(normalizedName);
                userRepository.save(u);
            }
            return u;
        }).orElseGet(() -> {
            User u = new User();
            u.setPin(normalizedPin);
            u.setName(normalizedName);
            u.setTheme("animals");

            Map<String, ProgressState> progress = new HashMap<>();
            progress.put("L1", defaultL1Progress());
            u.setProgress(progress);

            u.setDailyStats(new HashMap<>());
            return userRepository.save(u);
        });
    }

    /** Node semantics: today’s counts from user.dailyStats[today]; grand total from daily_summaries (+today if not rolled up). */
    public DailySummaryView getDaily(String userId) {
        User u = requireUser(userId);

        String today = LocalDate.now().toString(); // "YYYY-MM-DD"
        long todayCorrect = 0L;
        long todayActiveMs = 0L;

        Map<String, DailyStats> ds = u.getDailyStats();
        if (ds != null) {
            DailyStats t = ds.get(today);
            if (t != null) {
                todayCorrect = t.getCorrectCount();
                todayActiveMs = t.getTotalActiveMs();
            }
        }

        long grandTotalCorrect = 0L;
        var summaries = dailySummaryRepository.findByUserId(userId);
        for (DailySummary s : summaries) grandTotalCorrect += s.getCorrectCount();

        boolean hasTodaySummary = dailySummaryRepository.findByUserIdAndDate(userId, today).isPresent();
        if (!hasTodaySummary) {
            grandTotalCorrect += todayCorrect;
        }

        return new DailySummaryView(todayCorrect, todayActiveMs, grandTotalCorrect);
    }

    /** Node semantics: return the progression map as-is */
    public Map<String, ProgressState> getProgress(String userId) {
        User u = requireUser(userId);
        return u.getProgress() != null ? u.getProgress() : new HashMap<>();
    }

    /** Node semantics: reset progress to L1 (white unlocked), clear dailyStats, delete daily_summaries for user */
    public void resetUser(String userId) {
        User u = requireUser(userId);

        Map<String, ProgressState> progress = new HashMap<>();
        progress.put("L1", defaultL1Progress());
        u.setProgress(progress);

        u.setDailyStats(new HashMap<>());
        userRepository.save(u);

        var all = dailySummaryRepository.findByUserId(userId);
        if (!all.isEmpty()) dailySummaryRepository.deleteAll(all);
    }

    // ---- helpers ----
    public User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private ProgressState defaultL1Progress() {
        ProgressState p = new ProgressState();
        p.setLevel(1);
        p.setUnlocked(true);
        p.setComplete(false);

        BeltStatus white = new BeltStatus(); white.setUnlocked(true);
        p.setWhite(white);
        p.setYellow(new BeltStatus());
        p.setGreen(new BeltStatus());
        p.setBlue(new BeltStatus());
        p.setRed(new BeltStatus());
        p.setBrown(new BeltStatus());

        BlackProgress bp = new BlackProgress();
        bp.setUnlocked(false);
        bp.setCompletedDegrees(new ArrayList<>());
        p.setBlack(bp);
        return p;
    }

    /** View object for /user/daily */
    public static class DailySummaryView {
        public final long correctCountToday;
        public final long totalActiveMsToday;
        public final long grandTotalCorrect;
        public DailySummaryView(long c, long ms, long g) {
            this.correctCountToday = c; this.totalActiveMsToday = ms; this.grandTotalCorrect = g;
        }
    }
}
