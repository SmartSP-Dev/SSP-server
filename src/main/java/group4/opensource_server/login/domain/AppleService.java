package group4.opensource_server.login.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group4.opensource_server.jwt.AppleJwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AppleService {
    @Value("${apple.team_id}")
    private String teamId;

    @Value("${apple.key_id}")
    private String keyId;

    @Value("${apple.client_id}")
    private String clientId;

    @Value("${apple.private_key}")
    private String privateKeyContent;

    @Value("${apple.redirect_uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper;

    public Mono<JsonNode> exchangeCodeForTokens(String authorizationCode) {
        try {
            String clientSecret = AppleJwtUtil.generateClientSecret(teamId, keyId, clientId, privateKeyContent);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("code", authorizationCode);
            formData.add("grant_type", "authorization_code");
            formData.add("redirect_uri", redirectUri);

            return WebClient.create("https://appleid.apple.com").post()
                    .uri("/auth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        try {
                            return objectMapper.readTree(response);
                        } catch (Exception e) {
                            throw new RuntimeException("JSON 파싱 오류", e);
                        }
                    });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public Claims verifyIdToken(String idToken) {
        return Jwts.parserBuilder().build().parseClaimsJws(idToken).getBody();
    }
}
