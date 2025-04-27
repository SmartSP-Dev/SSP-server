package group4.opensource_server.quiz.domain;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QuizService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api.key}")
    private String apiKey;

    // PDF 텍스트 기반 퀴즈 생성 메서드
    public JSONObject generateQuizzesFromText(String text, String keyword, String questionType) {
        String trimmedText = text.length() > 3000 ? text.substring(0, 3000) : text;

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("다음 텍스트를 바탕으로 키워드 '")
                .append(keyword)
                .append("'에 관한 퀴즈를 생성하세요.\n")
                .append("퀴즈 유형: ").append(questionType).append("\n\n")
                .append("텍스트:\n").append(trimmedText).append("\n\n");

        switch (questionType.toLowerCase()) {
            case "객관식":
                promptBuilder.append("형식: { \"type\": \"multiple_choice\", \"question\": \"질문\", ")
                        .append("\"correct_answer\": \"정답\", \"incorrect_answers\": [\"오답1\", \"오답2\", \"오답3\"] }\n");
                break;
            case "ox":
                promptBuilder.append("형식: { \"type\": \"ox\", \"question\": \"OX 질문\", ")
                        .append("\"correct_answer\": \"O 또는 X\", \"incorrect_answers\": [] }\n");
                break;
            case "빈칸넣기":
                promptBuilder.append("형식: { \"type\": \"fill_in_the_blank\", \"question\": \"문장 ___\", ")
                        .append("\"correct_answer\": \"정답\", \"incorrect_answers\": [] }\n");
                break;
            default:
                promptBuilder.append("반드시 위의 형식 중 하나를 사용하세요.\n");
        }

        promptBuilder.append("\n반드시 JSON 형식으로 출력하세요.\n");

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
            messages.put(new JSONObject().put("role", "system").put("content", "You are a helpful assistant that generates quiz questions."));
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1500);
            requestBody.put("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, request, String.class);

            JSONObject rawResponse = new JSONObject(response.getBody());
            JSONArray choicesArray = rawResponse.getJSONArray("choices");
            JSONObject messageObj = choicesArray.getJSONObject(0).getJSONObject("message");
            String responseText = messageObj.getString("content");

            return new JSONObject(responseText);

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("error", "Failed to generate quiz: " + e.getMessage());
        }
    }
}