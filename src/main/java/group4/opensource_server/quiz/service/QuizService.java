package group4.opensource_server.quiz.service;

import group4.opensource_server.quiz.domain.*;
import group4.opensource_server.quiz.dto.QuizSubmitRequestDto;
import group4.opensource_server.quiz.dto.QuizSubmitResultDto;
import group4.opensource_server.quiz.dto.WeeklyQuizSummaryDto;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import group4.opensource_server.quiz.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionResultRepository questionResultRepository;

    @Transactional
    public List<Quiz> getQuizzesByUserId(Integer userId) {
        List<Quiz> quizzes = quizRepository.findByUserId(userId);
        for (Quiz quiz : quizzes) {
            for (QuizQuestion question : quiz.getQuestions()) {
                question.getIncorrectAnswers().size();
            }

            if (quiz.getStatus() == 3 &&
                    quiz.getLastReviewedAt() != null &&
                    quiz.getLastReviewedAt().isBefore(LocalDate.now())) {
                quiz.setStatus(2);
                quizRepository.save(quiz);
            }
        }
        return quizzes;
    }

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api.key}")
    private String apiKey;

    // PDF 텍스트 기반 퀴즈 생성 메서드
    public JSONObject generateQuizzesFromText(String text, String keyword, QuestionType questionType) {
        String trimmedText = text.length() > 15000 ? text.substring(0, 15000) : text;

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("[사용자 프롬프트]\n");
        promptBuilder.append("아래 텍스트를 참고하여, 키워드 '")
                .append(keyword)
                .append("'에 관한 퀴즈를 총 **10문제** 생성해 주세요.\n")
                .append("문제 유형: ").append(questionType).append("\n")
                .append("각 퀴즈 문항은 서로 다른 문맥, 세부 정보, 또는 응용 사례를 기반으로 생성해 주세요.\n")
                .append("- 유사하거나 반복적인 문장/형식이 없도록 주의해 주세요.\n")
                .append("- 각 문제에는 반드시 \"number\" 필드를 추가하여 1부터 10까지 번호를 부여해 주세요.\n")
                .append("- JSON 배열의 각 요소는 다음 필드를 포함해야 합니다:\n")
                .append("  - \"number\" (문제 번호, 정수)\n");

        if (questionType == QuestionType.FILL_BLANK) {
            promptBuilder.append("빈칸넣기 문제의 정답과 오답은 반드시 **단어**로만 구성되어야 하며, 문장이 답이 되지 않도록 하세요.\n");
        }

        promptBuilder.append("\n[참고 텍스트]\n").append(trimmedText).append("\n\n");

        switch (questionType) {
            case MULTIPLE_CHOICE:
                promptBuilder.append("출력 형식 예시: { \"type\": \"multiple_choice\", \"question\": \"질문\", ")
                        .append("\"correct_answer\": \"정답\", \"incorrect_answers\": [\"오답1\", \"오답2\", \"오답3\"] }\n");
                break;
            case OX:
                promptBuilder.append("출력 형식 예시: { \"type\": \"ox\", \"question\": \"OX 질문\", ")
                        .append("\"correct_answer\": \"O 또는 X\", \"incorrect_answers\": [] }\n");
                break;
            case FILL_BLANK:
                promptBuilder.append("출력 형식 예시: { \"type\": \"fill_blank\", \"question\": \"문장 ___\", ")
                        .append("\"correct_answer\": \"정답\", \"incorrect_answers\": [\"오답1\", \"오답2\", \"오답3\"] }\n");
                break;
            default:
                promptBuilder.append("반드시 위의 형식 중 하나를 사용하세요.\n");
        }

        promptBuilder.append("\n[중요] 반드시 JSON 배열 형태로만 출력하세요. 다른 설명이나 문장 없이, JSON 배열만 출력해야 합니다.\n");

        return callOpenAiApi(promptBuilder.toString());
    }

    // OpenAI API 호출 메서드
    private JSONObject callOpenAiApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + this.apiKey);
            headers.set("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "너는 퀴즈 문제를 생성하는 친절한 도우미야."));
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 3500);
            requestBody.put("temperature", 0.5);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, request, String.class);

            JSONObject rawResponse = new JSONObject(response.getBody());
            JSONArray choicesArray = rawResponse.getJSONArray("choices");
            JSONObject messageObj = choicesArray.getJSONObject(0).getJSONObject("message");
            String responseText = messageObj.getString("content");


            responseText = responseText.trim();
            if (responseText.startsWith("{")) {
                return new JSONObject(responseText);
            } else if (responseText.startsWith("[")) {
                JSONObject wrappedResponse = new JSONObject();
                wrappedResponse.put("quizzes", new JSONArray(responseText));
                return wrappedResponse;
            } else {
                throw new IllegalStateException("GPT 응답이 JSON 형식이 아님: " + responseText);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to generate quiz: " + e.getMessage());
            return errorResponse;
        }
    }

    @Transactional
    public Quiz createQuizWithQuestions(JSONObject openAiResponse, String title, String keyword, QuestionType questionType, User user) {
        // 1. Quiz 저장
        Quiz quiz = Quiz.builder()
                .user(user)
                .title(title)
                .keywords(keyword)
                .questionType(questionType)
                .status(1)  // 생성 직후 복습 완료 상태
                .reviewCount(0)
                .build();
        quizRepository.save(quiz);

        // 2. 퀴즈 문제 저장
        JSONArray quizzes = openAiResponse.getJSONArray("quizzes");
        for (int i = 0; i < quizzes.length(); i++) {
            JSONObject quizObject = quizzes.getJSONObject(i);

            // incorrect_answers를 JSONArray에서 List<String>으로 변환
            JSONArray incorrectArray = quizObject.optJSONArray("incorrect_answers");
            List<String> incorrectAnswers = new java.util.ArrayList<>();
            if (incorrectArray != null) {
                for (int j = 0; j < incorrectArray.length(); j++) {
                    incorrectAnswers.add(incorrectArray.getString(j));
                }
            }

            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .questionTitle(quizObject.optString("question", ""))
                    .questionContent("") // 지금은 비워둬도 됨
                    .quizNumber(i + 1)  // 1번부터 번호 부여
                    .correctAnswer(quizObject.optString("correct_answer", ""))
                    .incorrectAnswers(incorrectAnswers)
                    .questionType(questionType)
                    .build();

            quizQuestionRepository.save(question);
        }
        // 퀴즈에 문제 리스트를 붙여서 반환
        quiz.setQuestions(quizQuestionRepository.findByQuizId(quiz.getId()));
        return quiz;
    }

    @Transactional
    public QuizSubmitResultDto submitQuiz(QuizSubmitRequestDto request, User user) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));

        // 1. 퀴즈 시도 저장
        QuizAttempt attempt = quizAttemptRepository.save(
                QuizAttempt.builder()
                        .quiz(quiz)
                        .user(user)
                        .score(0) // 일단 0점, 아래에서 계산
                        .reviewCount(0)
                        .attemptTime(LocalDateTime.now())
                        .build()
        );

        int correctCount = 0;
        List<QuizSubmitResultDto.QuestionResult> resultList = new ArrayList<>();

        for (QuizSubmitRequestDto.AnswerDto answer : request.getAnswers()) {
            // quizNumber로 문제 찾기
            QuizQuestion question = quizQuestionRepository.findByQuizAndQuizNumber(quiz, answer.getQuizNumber())
                    .orElseThrow(() -> new IllegalArgumentException("해당 번호의 문제가 존재하지 않습니다."));

            boolean isCorrect = evaluateAnswer(question.getQuestionType(), answer.getUserAnswer(), question.getCorrectAnswer());

            if (isCorrect) correctCount++;

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

        // 점수 업데이트
        attempt.setScore(correctCount);
        quizAttemptRepository.save(attempt);

        quiz.setReviewCount(quiz.getReviewCount() + 1);     // 무조건 증가
        quiz.setLastReviewedAt(LocalDate.now());            // 오늘 복습했으므로
        quiz.setStatus(3);                                   // 복습 완료 상태

        quizRepository.save(quiz);

        // 결과 반환
        return QuizSubmitResultDto.builder()
                .totalQuestions(request.getAnswers().size())
                .correctAnswers(correctCount)
                .questionResults(resultList)
                .build();
    }

    private boolean evaluateAnswer(QuestionType type, String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) return false;

        return switch (type) {
            case FILL_BLANK -> normalize(userAnswer).equals(normalize(correctAnswer));
            case MULTIPLE_CHOICE, OX -> userAnswer.equalsIgnoreCase(correctAnswer);
            default -> false;
        };
    }

    private String normalize(String input) {
        return input.trim().toLowerCase();
    }


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
        long reviewed = thisWeekQuizzes.stream().filter(q -> q.getStatus() == 3).count();
        long notReviewed = thisWeekQuizzes.stream()
                .filter(q -> q.getStatus() == 2 || q.getStatus() == 1)
                .count();

        return new WeeklyQuizSummaryDto(total, reviewed, notReviewed);
    }

    public void deleteQuiz(User user, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));

        if (!quiz.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인이 생성한 퀴즈만 삭제할 수 있습니다.");
        }
        quizRepository.delete(quiz);
    }

    @Transactional
    public void updateQuizStatusesForNextDay() {
        List<Quiz> allQuizzes = quizRepository.findAll();

        for (Quiz quiz : allQuizzes) {
            if (quiz.getStatus() == 3 &&
                    quiz.getLastReviewedAt() != null &&
                    quiz.getLastReviewedAt().isBefore(LocalDate.now())) {

                quiz.setStatus(2); // 복습 필요 상태로 전환
                quizRepository.save(quiz);
            }
        }
    }

    @Transactional(readOnly = true)
    public QuizSubmitResultDto getLatestResultByQuizId(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("해당 퀴즈가 존재하지 않습니다."));

        QuizAttempt latestAttempt = quizAttemptRepository
                .findTopByQuizIdAndUserIdOrderByAttemptTimeDesc(quizId, user.getId())
                .orElseThrow(() -> new RuntimeException("해당 퀴즈에 대한 시도 기록이 없습니다."));

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
                .correctAnswers((int) questionResults.stream().filter(QuizSubmitResultDto.QuestionResult::isCorrect).count())
                .questionResults(questionResults)
                .build();
    }
}