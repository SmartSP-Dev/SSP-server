package group4.opensource_server.quiz.domain;

import group4.opensource_server.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizStatusScheduler {

    private final QuizService quizService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void runDailyQuizStatusUpdate() {
        quizService.updateQuizStatusesForNextDay();
    }
}