package group4.opensource_server.quiz.service;

import group4.opensource_server.quiz.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiClient {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    private static final int MAX_TOKENS = 3500;
    private static final double TEMPERATURE = 0.5;
    private static final String SYSTEM_PROMPT = "너는 퀴즈 문제를 생성하는 친절한 도우미야.";

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    /**
     * OpenAI API를 호출하여 퀴즈를 생성합니다.
     *
     * @param prompt 사용자가 작성한 프롬프트
     * @return 퀴즈 배열이 포함된 JSONObject
     */
    public JSONObject callApi(String prompt) {
        try {
            HttpHeaders headers = createHeaders();
            JSONObject requestBody = createRequestBody(prompt);
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return parseResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("OpenAI API 클라이언트 오류: {}", e.getMessage(), e);
            throw new ExternalApiException("GPT 요청 실패", e);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 예외 발생", e);
            throw new ExternalApiException("퀴즈 생성 실패", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.apiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private JSONObject createRequestBody(String prompt) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", SYSTEM_PROMPT));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("temperature", TEMPERATURE);

        return requestBody;
    }

    private JSONObject parseResponse(String responseBody) {
        JSONObject rawResponse = new JSONObject(responseBody);
        JSONArray choicesArray = rawResponse.getJSONArray("choices");
        JSONObject messageObj = choicesArray.getJSONObject(0).getJSONObject("message");
        String responseText = messageObj.getString("content");

        // 코드 블럭 제거
        responseText = removeCodeBlock(responseText).trim();

        if (responseText.startsWith("{")) {
            return new JSONObject(responseText);
        } else if (responseText.startsWith("[")) {
            JSONObject wrappedResponse = new JSONObject();
            wrappedResponse.put("quizzes", new JSONArray(responseText));
            return wrappedResponse;
        } else {
            throw new IllegalStateException("GPT 응답이 JSON 형식이 아님: " + responseText);
        }
    }

    private String removeCodeBlock(String text) {
        if (text.startsWith("```json")) {
            return text.replaceFirst("^```json\\s*", "").replaceFirst("\\s*```\\s*$", "");
        }
        return text;
    }
}
