package com.infinityisland.service;

import com.infinityisland.controller.QuizDtos;
import com.infinityisland.dao.DailySummary;
import com.infinityisland.dao.GeneratedQuestion;
import com.infinityisland.dao.QuizRun;
import com.infinityisland.dao.user.DailyStats;
import com.infinityisland.dao.user.User;

import com.infinityisland.repositories.DailySummaryRepository;
import com.infinityisland.repositories.GeneratedQuestionRepository;
import com.infinityisland.repositories.QuizRunRepository;
import com.infinityisland.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRunRepository quizRunRepo;
    private final GeneratedQuestionRepository gqRepo;
    private final UserRepository userRepo;
    private final DailySummaryRepository dailySummaryRepo;

    public QuizService(QuizRunRepository quizRunRepo,
                       GeneratedQuestionRepository gqRepo,
                       UserRepository userRepo,
                       DailySummaryRepository dailySummaryRepo) {
        this.quizRunRepo = quizRunRepo;
        this.gqRepo = gqRepo;
        this.userRepo = userRepo;
        this.dailySummaryRepo = dailySummaryRepo;
    }

    // ---------------------- PREPARE ----------------------

    public QuizDtos.PrepareResponse prepare(String userId, QuizDtos.PrepareRequest req) {
        String op = StringUtils.hasText(req.operation()) ? req.operation() : "add";
        String level = req.level();
        String beltOrDegree = req.beltOrDegree();

        // Load a bank for (op, level, beltOrDegree)
        List<GeneratedQuestion> bank =
                gqRepo.findByOperationAndLevelAndBeltOrDegree(op, level, beltOrDegree);
        if (bank.isEmpty()) {
            bank = synthesizeQuestions(op, level, beltOrDegree, 10);
            gqRepo.saveAll(bank);
        }

        // Create run
        QuizRun run = new QuizRun();
        run.setUserId(userId);
        run.setOperation(op);
        run.setLevel(level);
        run.setBeltOrDegree(beltOrDegree);
        run.setStatus("prepared");
        run.setCreatedAt(Instant.now());
        run.setItemIds(bank.stream().map(GeneratedQuestion::getId).collect(Collectors.toList()));
        run.setCurrentIndex(0);
        run.setCorrect(0);
        run.setWrong(0);
        run.setTotalActiveMs(0L);
        quizRunRepo.save(run);

        // practice list (empty for now)
        return new QuizDtos.PrepareResponse(run.getId(), List.of());
    }

    // ---------------------- START ----------------------

    public QuizDtos.StartResponse start(String userId, QuizDtos.StartRequest req) {
        QuizRun run = guardRunOwnedByUser(req.quizRunId(), userId);
        // move to in-progress if still prepared
        if (!"in-progress".equals(run.getStatus())) {
            run.setStatus("in-progress");
            quizRunRepo.save(run);
        }
        QuizDtos.QuestionDto q = mapQuestion(run, run.getCurrentIndex());
        return new QuizDtos.StartResponse(q);
    }

    // ---------------------- ANSWER ----------------------

    public QuizDtos.AnswerOrPracticeResponse answer(String userId, QuizDtos.AnswerRequest req) {
        QuizRun run = guardRunOwnedByUser(req.quizRunId(), userId);
        ensureActive(run);

        String currentQid = getCurrentQuestionId(run);
        if (!Objects.equals(currentQid, req.questionId())) {
            // client out of sync → send current
            return new QuizDtos.AnswerOrPracticeResponse(false, false,
                    mapQuestion(run, run.getCurrentIndex()), null,
                    run.getCorrect(), null);
        }

        GeneratedQuestion gq = getQuestionById(currentQid);
        boolean isCorrect = req.answer() != null && req.answer().intValue() == gq.getCorrectAnswer();

        long deltaMs = req.responseMs() != null ? req.responseMs() : 0L;
        run.setTotalActiveMs(run.getTotalActiveMs() + deltaMs);

        if (isCorrect) {
            run.setCorrect(run.getCorrect() + 1);

            boolean isLast = run.getCurrentIndex() >= run.getItemIds().size() - 1;
            if (isLast) {
                run.setStatus("completed");
                quizRunRepo.save(run);

                QuizDtos.DailyStatsDto daily = addToDaily(userId, 1, deltaMs);
                return new QuizDtos.AnswerOrPracticeResponse(true, null, null, null,
                        run.getCorrect(), daily);
            } else {
                run.setCurrentIndex(run.getCurrentIndex() + 1);
                quizRunRepo.save(run);

                addToDaily(userId, 1, deltaMs);

                QuizDtos.QuestionDto next = mapQuestion(run, run.getCurrentIndex());
                return new QuizDtos.AnswerOrPracticeResponse(null, null, next, null,
                        run.getCorrect(), null);
            }
        } else {
            // wrong → practice
            QuizDtos.QuestionDto practice = toDto(gq);
            quizRunRepo.save(run); // keep state
            return new QuizDtos.AnswerOrPracticeResponse(null, null, null, practice,
                    run.getCorrect(), null);
        }
    }

    // ---------------------- INACTIVITY ----------------------

    public QuizDtos.AnswerOrPracticeResponse inactivity(String userId, QuizDtos.InactivityRequest req) {
        QuizRun run = guardRunOwnedByUser(req.quizRunId(), userId);
        ensureActive(run);

        String currentQid = getCurrentQuestionId(run);
        if (!Objects.equals(currentQid, req.questionId())) {
            return new QuizDtos.AnswerOrPracticeResponse(false, false,
                    mapQuestion(run, run.getCurrentIndex()), null,
                    run.getCorrect(), null);
        }

        // black belts immediately complete on inactivity (parity with Node’s “hard mode”)
        if (run.getBeltOrDegree() != null && run.getBeltOrDegree().toLowerCase().startsWith("black")) {
            run.setStatus("completed");
            quizRunRepo.save(run);
            QuizDtos.DailyStatsDto daily = addToDaily(userId, 0, 0);
            return new QuizDtos.AnswerOrPracticeResponse(true, null, null, null,
                    run.getCorrect(), daily);
        }

        // otherwise provide practice for the same problem
        QuizDtos.QuestionDto practice = toDto(getQuestionById(currentQid));
        return new QuizDtos.AnswerOrPracticeResponse(null, null, null, practice,
                run.getCorrect(), null);
    }

    // ---------------------- PRACTICE ANSWER ----------------------

    public QuizDtos.AnswerOrPracticeResponse practiceAnswer(String userId, QuizDtos.PracticeAnswerRequest req) {
        QuizRun run = guardRunOwnedByUser(req.quizRunId(), userId);
        ensureActive(run);

        String currentQid = getCurrentQuestionId(run);
        GeneratedQuestion gq = getQuestionById(currentQid);
        boolean correct = req.answer() != null && req.answer().intValue() == gq.getCorrectAnswer();

        boolean isLast = run.getCurrentIndex() >= run.getItemIds().size() - 1;

        if (correct) {
            if (isLast) {
                // end quiz after last question’s practice
                run.setStatus("completed");
                quizRunRepo.save(run);
                QuizDtos.DailyStatsDto daily = addToDaily(userId, 0, 0);
                return new QuizDtos.AnswerOrPracticeResponse(true, null, null, null,
                        run.getCorrect(), daily);
            } else {
                // resume main quiz on next item
                run.setCurrentIndex(run.getCurrentIndex() + 1);
                quizRunRepo.save(run);
                QuizDtos.QuestionDto next = mapQuestion(run, run.getCurrentIndex());
                return new QuizDtos.AnswerOrPracticeResponse(null, true, next, null,
                        run.getCorrect(), null);
            }
        } else {
            // keep practicing same item
            QuizDtos.QuestionDto practice = toDto(gq);
            return new QuizDtos.AnswerOrPracticeResponse(null, null, null, practice,
                    run.getCorrect(), null);
        }
    }

    // ---------------------- COMPLETE ----------------------

    public QuizDtos.AnswerOrPracticeResponse complete(String userId, QuizDtos.CompleteRequest req) {
        QuizRun run = guardRunOwnedByUser(req.quizRunId(), userId);
        if (!"completed".equals(run.getStatus())) {
            run.setStatus("completed");
            quizRunRepo.save(run);
        }
        QuizDtos.DailyStatsDto daily = addToDaily(userId, 0, 0);
        return new QuizDtos.AnswerOrPracticeResponse(true, null, null, null,
                run.getCorrect(), daily);
    }

    // ---------------------- helpers ----------------------

    private QuizRun guardRunOwnedByUser(String runId, String userId) {
        return quizRunRepo.findById(runId)
                .filter(r -> Objects.equals(r.getUserId(), userId))
                .orElseThrow(() -> new NoSuchElementException("QuizRun not found"));
    }

    private void ensureActive(QuizRun run) {
        if (!"prepared".equals(run.getStatus()) && !"in-progress".equals(run.getStatus())) {
            throw new IllegalStateException("QuizRun is not active");
        }
        if (!"in-progress".equals(run.getStatus())) {
            run.setStatus("in-progress");
            quizRunRepo.save(run);
        }
    }

    private String getCurrentQuestionId(QuizRun run) {
        List<String> ids = run.getItemIds();
        if (ids == null || ids.isEmpty()) throw new IllegalStateException("Run has no items");
        int idx = Math.min(Math.max(run.getCurrentIndex(), 0), ids.size() - 1);
        return ids.get(idx);
    }

    private GeneratedQuestion getQuestionById(String id) {
        return gqRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found"));
    }

    private QuizDtos.QuestionDto mapQuestion(QuizRun run, int index) {
        String qid = run.getItemIds().get(index);
        return toDto(getQuestionById(qid));
    }

    private QuizDtos.QuestionDto toDto(GeneratedQuestion gq) {
        return new QuizDtos.QuestionDto(
                gq.getId(),
                gq.getOperation(),
                gq.getLevel(),
                gq.getBeltOrDegree(),
                gq.getA(),
                gq.getB(),
                gq.getQuestion(),
                gq.getCorrectAnswer(),
                gq.getChoices() // List<Integer>
        );
    }

    private QuizDtos.DailyStatsDto addToDaily(String userId, long addCorrect, long addMs) {
        User u = userRepo.findById(userId).orElseThrow();
        String today = LocalDate.now().toString();

        Map<String, DailyStats> map = u.getDailyStats();
        if (map == null) {
            map = new HashMap<>();
            u.setDailyStats(map);
        }

        DailyStats ds = map.get(today);
        if (ds == null) {
            ds = new DailyStats();
            ds.setDate(today);
            ds.setCorrectCount(0L);
            ds.setTotalActiveMs(0L);
            map.put(today, ds);
        }
        ds.setCorrectCount(ds.getCorrectCount() + addCorrect);
        ds.setTotalActiveMs(ds.getTotalActiveMs() + addMs);
        userRepo.save(u);

        // Keep daily_summaries in sync (as your Node did)
        DailySummary sum = dailySummaryRepo.findByUserIdAndDate(userId, today).orElseGet(() -> {
            DailySummary d = new DailySummary();
            d.setUserId(userId);
            d.setDate(today);
            d.setCorrectCount(0L);
            d.setTotalActiveMs(0L);
            d.setReportSentMarker(false);
            return d;
        });
        sum.setCorrectCount(sum.getCorrectCount() + addCorrect);
        sum.setTotalActiveMs(sum.getTotalActiveMs() + addMs);
        dailySummaryRepo.save(sum);

        long grand = dailySummaryRepo.findByUserId(userId).stream()
                .mapToLong(DailySummary::getCorrectCount).sum();

        return new QuizDtos.DailyStatsDto(ds.getCorrectCount(), ds.getTotalActiveMs(), grand);
    }

    private List<GeneratedQuestion> synthesizeQuestions(String op, String level, String belt, int n) {
        Random r = new Random();
        List<GeneratedQuestion> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int a = r.nextInt(10) + 1, b = r.nextInt(10) + 1;
            int ans;
            String text;
            int aa = a, bb = b;

            switch (op) {
                case "sub":
                    ans = a - b;
                    text = a + " - " + b + " = ?";
                    break;
                case "mul":
                    ans = a * b;
                    text = a + " × " + b + " = ?";
                    break;
                case "div":
                    ans = (b == 0 ? 0 : a);
                    text = (a * b) + " ÷ " + b + " = ?";
                    aa = a * b;
                    bb = b;
                    break;
                default:
                    ans = a + b;
                    text = a + " + " + b + " = ?";
                    break;
            }

            GeneratedQuestion gq = new GeneratedQuestion();
            gq.setOperation(op);
            gq.setLevel(level);
            gq.setBeltOrDegree(belt);
            gq.setA(aa);
            gq.setB(bb);
            gq.setQuestion(text);
            gq.setCorrectAnswer(ans);
            gq.setChoices(java.util.List.of(ans, ans + 1, ans - 1, ans + 2));
            gq.setSource("current");
            out.add(gq);
        }
        return out;
    }
}