package com.infinityisland.service;

import com.infinityisland.dao.DailySummary;
import com.infinityisland.dao.user.*;
import com.infinityisland.repositories.DailySummaryRepository;
import com.infinityisland.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
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
        if (!StringUtils.hasText(pin)) throw new IllegalArgumentException("pin is required");
        String normalizedPin = pin.trim();
        String normalizedName = StringUtils.hasText(name) ? name.trim() : null;

        return userRepository.findByPin(normalizedPin).map(u -> {
            // Only set name on first login if blank
            if (!StringUtils.hasText(u.getName()) && StringUtils.hasText(normalizedName)) {
                u.setName(normalizedName);
                userRepository.save(u);
            }
            return u;
        }).orElseGet(() -> {
            if (!StringUtils.hasText(normalizedName)) {
                throw new IllegalArgumentException("name is required for new user signup");
            }
            User u = new User();
            u.setPin(normalizedPin);
            u.setName(normalizedName);

            // Do NOT set theme by default (Node leaves null until set once)
            // u.setTheme("animals");

            Map<String, ProgressState> progress = new HashMap<>();
            progress.put("L1", defaultL1Progress());
            u.setProgress(progress);

            u.setDailyStats(new HashMap<>());
            return userRepository.save(u);
        });
    }

    /**
     * Node semantics: today’s counts from user.dailyStats[today]; grand total from daily_summaries (+today if not rolled up).
     */
    public DailySummaryView getDaily(String userId) {
        User u = requireUser(userId);

        String today = LocalDate.now().toString(); // YYYY-MM-DD
        long todayCorrect = 0L;
        long todayActiveMs = 0L;

        Map<String, DailyStats> ds = u.getDailyStats();
        if (ds != null) {
            DailyStats t = ds.get(today);
            if (t != null) {
                todayCorrect = t.getCorrectCount() != null ? t.getCorrectCount() : 0L;
                todayActiveMs = t.getTotalActiveMs() != null ? t.getTotalActiveMs() : 0L;
            }
        }

        long rolledUp = dailySummaryRepository.findByUserId(userId)
                .stream()
                .map(DailySummary::getCorrectCount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        long grandTotal = rolledUp + todayCorrect;

        return new DailySummaryView(todayCorrect, todayActiveMs, grandTotal);
    }

    /**
     * Update and persist login streak + lastLoginDate using Pacific time. Returns new streak.
     */
    public int updateLoginStreakPacific(User u) {
        ZoneId pacific = ZoneId.of("America/Los_Angeles");
        String today = LocalDate.now(pacific).toString();
        String yesterday = LocalDate.now(pacific).minusDays(1).toString();

        Integer streak = u.getCurrentStreak() != null ? u.getCurrentStreak() : 0;
        String last = u.getLastLoginDate();

        if (!today.equals(last)) {
            if (yesterday.equals(last)) {
                streak = streak + 1;
            } else {
                streak = 1;
            }
            u.setCurrentStreak(streak);
            u.setLastLoginDate(today);
            userRepository.save(u);
        }
        return u.getCurrentStreak() != null ? u.getCurrentStreak() : 0;
    }

    /**
     * Set theme once (first-time); subsequent calls error. Returns the saved theme.
     */
    public String setThemeFirstTime(String userId, String themeKey) {
        if (!StringUtils.hasText(themeKey)) throw new IllegalArgumentException("Theme key required");
        User u = requireUser(userId);
        if (StringUtils.hasText(u.getTheme())) {
            throw new IllegalStateException("Theme already set");
        }
        u.setTheme(themeKey.trim());
        userRepository.save(u);
        return u.getTheme();
    }

    public Map<String, ProgressState> getProgress(String userId) {
        return requireUser(userId).getProgress();
    }

    public void resetUser(String userId) {
        User u = requireUser(userId);
        u.setProgress(new HashMap<>());
        u.getProgress().put("L1", defaultL1Progress());
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

        BeltStatus white = new BeltStatus();
        white.setUnlocked(true);
        p.setWhite(white);
        p.setYellow(new BeltStatus());
        p.setGreen(new BeltStatus());
        p.setBlue(new BeltStatus());
        p.setRed(new BeltStatus());
        p.setBrown(new BeltStatus());

        BlackProgress bp = new BlackProgress();
        bp.setUnlocked(false);
        bp.setCompletedDegrees(new java.util.ArrayList<>());
        p.setBlack(bp);
        return p;
    }

    /**
     * View object for /user/daily
     */
    public static class DailySummaryView {
        public final long correctCountToday;
        public final long totalActiveMsToday;
        public final long grandTotalCorrect;

        public DailySummaryView(long c, long ms, long g) {
            this.correctCountToday = c;
            this.totalActiveMsToday = ms;
            this.grandTotalCorrect = g;
        }
    }
}