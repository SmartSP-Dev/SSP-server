package group4.opensource_server.quiz.service;

import group4.opensource_server.quiz.domain.QuestionType;
import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.domain.QuizQuestion;
import group4.opensource_server.quiz.repository.QuizQuestionRepository;
import group4.opensource_server.quiz.repository.QuizRepository;
import group4.opensource_server.user.domain.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    @Autowired
    public QuizService(QuizRepository quizRepository, QuizQuestionRepository quizQuestionRepository) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
    }

    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesByUserId(Integer userId) {
        List<Quiz> quizzes = quizRepository.findByUserId(userId);
        for (Quiz quiz : quizzes) {
            for (QuizQuestion question : quiz.getQuestions()) {
                question.getIncorrectAnswers().size(); // Force initialize the collection
            }
        }
        return quizzes;
    }

    // 특정 퀴즈에 해당하는 문제 목록 조회
    public List<QuizQuestion> getQuizQuestionsByQuizId(Long quizId) {
        return quizQuestionRepository.findByQuizId(quizId);
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
}