package group4.opensource_server.login.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import group4.opensource_server.login.dto.AppleTokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AppleClient {

    private final AppleJwtValidator jwtValidator;

    @Value("${apple.client_id}")
    private String clientId;

    @Value("${apple.redirect_uri}")
    private String redirectUri;

    public AppleTokenResponseDto requestToken(String code) {
        String clientSecret = jwtValidator.generateClientSecret();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        HttpEntity<?> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<AppleTokenResponseDto> response = restTemplate.postForEntity(
                "https://appleid.apple.com/auth/token", request, AppleTokenResponseDto.class
        );
        return response.getBody();
    }
}
