package group4.opensource_server.quiz.service;

import group4.opensource_server.quiz.domain.*;
import group4.opensource_server.quiz.dto.QuizSubmitRequestDto;
import group4.opensource_server.quiz.dto.QuizSubmitResultDto;
import group4.opensource_server.quiz.dto.WeeklyQuizSummaryDto;
import group4.opensource_server.quiz.exception.QuizNotFoundException;
import group4.opensource_server.quiz.exception.UnauthorizedQuizAccessException;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static group4.opensource_server.quiz.domain.QuizConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionResultRepository questionResultRepository;

    private final OpenAiClient openAiClient;
    private final QuizPromptBuilder promptBuilder;
    private final QuizGrader quizGrader;

    /**
     * 사용자 ID로 퀴즈 목록을 조회합니다 (읽기 전용).
     */
    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesByUserId(Integer userId) {
        return quizRepository.findByUserId(userId);
    }

    /**
     * 만료된 퀴즈 상태를 복습 필요로 업데이트합니다 (Bulk update).
     */
    @Transactional
    public void updateExpiredQuizStatuses() {
        int updatedCount = quizRepository.updateExpiredQuizStatuses(LocalDate.now());
        log.info("복습 필요 상태로 전환된 퀴즈: {}개", updatedCount);
    }

    /**
     * 텍스트 기반으로 퀴즈를 생성합니다.
     */
    public JSONObject generateQuizzesFromText(String text, String keyword, QuestionType questionType) {
        JSONArray quizzes = new JSONArray();
        int textLength = text.length();

        if (textLength <= SMALL_TEXT_THRESHOLD) {
            // 전체 텍스트에서 10문제
            quizzes = getQuizChunk(text, keyword, questionType, 1, SMALL_TEXT_QUESTIONS);

        } else if (textLength <= MEDIUM_TEXT_THRESHOLD) {
            // 2등분 → 5문제씩
            String part1 = text.substring(0, SMALL_TEXT_THRESHOLD);
            String part2 = text.substring(SMALL_TEXT_THRESHOLD);

            quizzes.putAll(getQuizChunk(part1, keyword, questionType, 1, MEDIUM_TEXT_QUESTIONS_PER_PART));
            quizzes.putAll(getQuizChunk(part2, keyword, questionType, 6, SMALL_TEXT_QUESTIONS));

        } else {
            // 3등분 → 3 + 3 + 4문제
            String part1 = text.substring(0, SMALL_TEXT_THRESHOLD);
            String part2 = text.substring(SMALL_TEXT_THRESHOLD, MEDIUM_TEXT_THRESHOLD);
            String part3 = text.substring(MEDIUM_TEXT_THRESHOLD, Math.min(LARGE_TEXT_MAX, text.length()));

            quizzes.putAll(getQuizChunk(part1, keyword, questionType, 1, LARGE_TEXT_QUESTIONS_PART1));
            quizzes.putAll(getQuizChunk(part2, keyword, questionType, 4, LARGE_TEXT_QUESTIONS_PART1 + LARGE_TEXT_QUESTIONS_PART2));
            quizzes.putAll(getQuizChunk(part3, keyword, questionType, 7, SMALL_TEXT_QUESTIONS));
        }

        JSONObject result = new JSONObject();
        result.put("status", "success");
        result.put("message", "퀴즈 생성 성공");
        result.put("quizzes", quizzes);
        return result;
    }

    /**
     * 텍스트 청크에서 퀴즈를 생성합니다.
     */
    private JSONArray getQuizChunk(String textChunk, String keyword, QuestionType questionType, int startNum, int endNum) {
        String prompt = promptBuilder.buildPrompt(textChunk, keyword, questionType, startNum, endNum);
        JSONObject response = openAiClient.callApi(prompt);
        return response.optJSONArray("quizzes") != null ? response.getJSONArray("quizzes") : new JSONArray();
    }

    /**
     * OpenAI 응답으로부터 퀴즈와 문제들을 생성하여 저장합니다.
     */
    @Transactional
    public Quiz createQuizWithQuestions(JSONObject openAiResponse, String title, String keyword, QuestionType questionType, User user) {
        // 1. Quiz 저장
        Quiz quiz = Quiz.builder()
                .user(user)
                .title(title)
                .keywords(keyword)
                .questionType(questionType)
                .build();
        quizRepository.save(quiz);

        // 2. 퀴즈 문제 저장
        JSONArray quizzes = openAiResponse.getJSONArray("quizzes");
        List<QuizQuestion> questions = new ArrayList<>();

        for (int i = 0; i < quizzes.length(); i++) {
            JSONObject quizObject = quizzes.getJSONObject(i);
            List<String> incorrectAnswers = extractIncorrectAnswers(quizObject);

            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .questionTitle(quizObject.optString("question", ""))
                    .questionContent("")
                    .quizNumber(i + 1)
                    .correctAnswer(quizObject.optString("correct_answer", ""))
                    .incorrectAnswers(incorrectAnswers)
                    .questionType(questionType)
                    .build();

            questions.add(quizQuestionRepository.save(question));
        }

        return quiz;
    }

    /**
     * JSONObject에서 오답 배열을 추출합니다.
     */
    private List<String> extractIncorrectAnswers(JSONObject quizObject) {
        JSONArray incorrectArray = quizObject.optJSONArray("incorrect_answers");
        List<String> incorrectAnswers = new ArrayList<>();

        if (incorrectArray != null) {
            for (int j = 0; j < incorrectArray.length(); j++) {
                incorrectAnswers.add(incorrectArray.getString(j));
            }
        }

        return incorrectAnswers;
    }

    /**
     * 퀴즈 제출을 처리하고 채점 결과를 반환합니다.
     */
    @Transactional
    public QuizSubmitResultDto submitQuiz(QuizSubmitRequestDto request, User user) {
        Quiz quiz = findQuizById(request.getQuizId());
        QuizAttempt attempt = createAttempt(quiz, user);

        List<QuizSubmitResultDto.QuestionResult> resultList = gradeAnswers(quiz, request.getAnswers(), attempt);
        int correctCount = countCorrectAnswers(resultList);

        updateAttemptScore(attempt, correctCount);
        markQuizAsReviewed(quiz);

        return buildResult(request.getAnswers().size(), correctCount, resultList);
    }

    private Quiz findQuizById(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));
    }

    private QuizAttempt createAttempt(Quiz quiz, User user) {
        return quizAttemptRepository.save(
                QuizAttempt.builder()
                        .quiz(quiz)
                        .user(user)
                        .score(0)
                        .reviewCount(0)
                        .attemptTime(LocalDateTime.now())
                        .build()
        );
    }

    private List<QuizSubmitResultDto.QuestionResult> gradeAnswers(
            Quiz quiz,
            List<QuizSubmitRequestDto.AnswerDto> answers,
            QuizAttempt attempt) {

        List<QuizSubmitResultDto.QuestionResult> resultList = new ArrayList<>();

        for (QuizSubmitRequestDto.AnswerDto answer : answers) {
            QuizQuestion question = quizQuestionRepository.findByQuizAndQuizNumber(quiz, answer.getQuizNumber())
                    .orElseThrow(() -> new IllegalArgumentException("해당 번호의 문제가 존재하지 않습니다."));

            boolean isCorrect = quizGrader.evaluateAnswer(
                    question.getQuestionType(),
                    answer.getUserAnswer(),
                    question.getCorrectAnswer()
            );

            // 문항별 결과 저장
            questionResultRepository.save(
                    QuestionResult.builder()
                            .quizAttempt(attempt)
                            .quiz(quiz)
                            .quizQuestion(question)
                            .userAnswer(answer.getUserAnswer())
                            .isCorrect(isCorrect)
                            .build()
            );

            resultList.add(new QuizSubmitResultDto.QuestionResult(
                    question.getQuizNumber(),
                    question.getQuestionTitle(),
                    answer.getUserAnswer(),
                    question.getCorrectAnswer(),
                    isCorrect
            ));
        }

        return resultList;
    }

    private int countCorrectAnswers(List<QuizSubmitResultDto.QuestionResult> resultList) {
        return (int) resultList.stream()
                .filter(QuizSubmitResultDto.QuestionResult::isCorrect)
                .count();
    }

    private void updateAttemptScore(QuizAttempt attempt, int correctCount) {
        attempt.updateScore(correctCount);
        quizAttemptRepository.save(attempt);
    }

    private void markQuizAsReviewed(Quiz quiz) {
        quiz.completeReview();
        quizRepository.save(quiz);
    }

    private QuizSubmitResultDto buildResult(int totalQuestions, int correctCount, List<QuizSubmitResultDto.QuestionResult> resultList) {
        return QuizSubmitResultDto.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers(correctCount)
                .questionResults(resultList)
                .build();
    }

    /**
     * 주간 퀴즈 요약 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public WeeklyQuizSummaryDto getWeeklyQuizSummary(User user) {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        List<Quiz> allUserQuizzes = quizRepository.findByUserId(user.getId());

        // 이번 주에 생성되었거나 마지막 복습된 퀴즈만 필터링
        List<Quiz> thisWeekQuizzes = allUserQuizzes.stream()
                .filter(quiz -> {
                    LocalDate baseDate = quiz.getLastReviewedAt() != null
                            ? quiz.getLastReviewedAt()
                            : quiz.getCreatedAt();
                    return baseDate != null && !baseDate.isBefore(startOfWeek);
                })
                .toList();

        long total = thisWeekQuizzes.size();
        long reviewed = thisWeekQuizzes.stream()
                .filter(Quiz::isReviewed)
                .count();
        long notReviewed = thisWeekQuizzes.stream()
                .filter(q -> !q.isReviewed())
                .count();

        return new WeeklyQuizSummaryDto(total, reviewed, notReviewed);
    }

    /**
     * 퀴즈를 삭제합니다 (Bulk delete 사용).
     */
    @Transactional
    public void deleteQuiz(User user, Long quizId) {
        Quiz quiz = findQuizById(quizId);
        validateQuizOwnership(user, quiz);

        // Bulk delete: 순서 중요 (외래 키 관계)
        questionResultRepository.deleteByQuiz(quiz);
        quizAttemptRepository.deleteByQuiz(quiz);
        quizQuestionRepository.deleteByQuiz(quiz);
        quizRepository.delete(quiz);

        log.info("퀴즈 삭제 완료: quizId={}, userId={}", quizId, user.getId());
    }

    private void validateQuizOwnership(User user, Quiz quiz) {
        if (!quiz.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedQuizAccessException("본인이 생성한 퀴즈만 삭제할 수 있습니다.");
        }
    }

    /**
     * 퀴즈의 가장 최근 채점 결과를 조회합니다.
     */
    @Transactional(readOnly = true)
    public QuizSubmitResultDto getLatestResultByQuizId(Long quizId, User user) {
        Quiz quiz = findQuizById(quizId);

        QuizAttempt latestAttempt = quizAttemptRepository
                .findTopByQuizIdAndUserIdOrderByAttemptTimeDesc(quizId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 퀴즈에 대한 시도 기록이 없습니다."));

        List<QuestionResult> results = questionResultRepository.findByQuizAttemptId(latestAttempt.getId());

        List<QuizSubmitResultDto.QuestionResult> questionResults = results.stream()
                .map(r -> new QuizSubmitResultDto.QuestionResult(
                        r.getQuizQuestion().getQuizNumber(),
                        r.getQuizQuestion().getQuestionTitle(),
                        r.getUserAnswer(),
                        r.getQuizQuestion().getCorrectAnswer(),
                        r.isCorrect()
                ))
                .toList();

        return QuizSubmitResultDto.builder()
                .totalQuestions(questionResults.size())
                .correctAnswers((int) questionResults.stream()
                        .filter(QuizSubmitResultDto.QuestionResult::isCorrect)
                        .count())
                .questionResults(questionResults)
                .build();
    }
}
