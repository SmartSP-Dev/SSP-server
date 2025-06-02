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
        JSONArray quizzes = new JSONArray();
        int textLength = text.length();

        if (textLength <= 10000) {
            // 전체 텍스트에서 10문제
            quizzes = getQuizChunk(text, keyword, questionType, 1, 10);

        } else if (textLength <= 20000) {
            // 2등분 → 5문제씩
            String part1 = text.substring(0, 10000);
            String part2 = text.substring(10000);

            quizzes.putAll(getQuizChunk(part1, keyword, questionType, 1, 5));
            quizzes.putAll(getQuizChunk(part2, keyword, questionType, 6, 10));

        } else {
            // 3등분 → 3 + 3 + 4문제
            String part1 = text.substring(0, 10000);
            String part2 = text.substring(10000, 20000);
            String part3 = text.substring(20000, Math.min(30000, text.length()));

            quizzes.putAll(getQuizChunk(part1, keyword, questionType, 1, 3));
            quizzes.putAll(getQuizChunk(part2, keyword, questionType, 4, 6));
            quizzes.putAll(getQuizChunk(part3, keyword, questionType, 7, 10));
        }

        JSONObject result = new JSONObject();
        result.put("quizzes", quizzes);
        return result;
    }

    private JSONArray getQuizChunk(String textChunk, String keyword, QuestionType questionType, int startNum, int endNum) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("[사용자 프롬프트]\n");
        promptBuilder.append("아래 텍스트를 참고하여, 해당 내용을 학습하는 학습자가 이해도를 점검할 수 있도록 퀴즈를 총 **")
                .append(endNum - startNum + 1).append("문제** 생성해 주세요.\n")
                .append("문제 유형: ").append(questionType).append("\n")
                .append("절대로 정답이 같은 단어로 반복되지 않도록 주의해 주세요.\n")
                .append("정답은 문맥에서 중요하게 언급된 용어, 개념을 사용해 주세요.\n")
                .append("유사하거나 반복적인 문장/형식 절대 없이 다양하게 구성해 주세요.\n")
                .append("각 문제에는 반드시 \"number\" 필드를 추가하여 번호를 ").append(startNum).append("~").append(endNum).append(" 범위로 부여해 주세요.\n");

        if (questionType == QuestionType.FILL_BLANK) {
            promptBuilder.append("※ 규칙 (반드시 지켜야 함):\n");
            promptBuilder.append("- 정답과 오답은 모두 한글 또는 영어 단어여야 하며, 문장이나 구문은 절대 포함하지 마세요.\n");
            promptBuilder.append("- 정답은 반드시 용언(한다, 이다 등)을 제외한 명사형이어야 하며,\n");
            promptBuilder.append("  문제 문장의 빈칸에도 해당 단어만 들어가 자연스럽게 완성되어야 합니다.\n");
            promptBuilder.append("- 예: \"상향한다\" → 문제는 \"___한다\", 정답은 \"상향\"\n");
        }

        promptBuilder.append("\n[참고 텍스트]\n").append(textChunk).append("\n\n");

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
                        .append("\"correct_answer\": \"정답\" }\n");
                break;
            default:
                promptBuilder.append("반드시 위의 형식 중 하나를 사용하세요.\n");
        }

        promptBuilder.append("\n[중요] 반드시 JSON 배열 형태로만 출력하세요. 다른 설명이나 문장 없이, JSON 배열만 출력해야 합니다.\n");

        JSONObject response = callOpenAiApi(promptBuilder.toString());
        return response.optJSONArray("quizzes") != null ? response.getJSONArray("quizzes") : new JSONArray();
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

    @Transactional
    public void deleteQuiz(User user, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));

        if (!quiz.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인이 생성한 퀴즈만 삭제할 수 있습니다.");
        }

        // 1. 퀴즈 시도 결과 삭제 (question_results)
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz(quiz);
        for (QuizAttempt attempt : attempts) {
            questionResultRepository.deleteByQuizAttempt(attempt);
        }

        // 2. 퀴즈 시도 삭제 (quiz_attempts)
        quizAttemptRepository.deleteByQuiz(quiz);

        // 3. 퀴즈 문제 삭제 (quiz_questions)
        quizQuestionRepository.deleteByQuiz(quiz);

        // 4. 퀴즈 삭제
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