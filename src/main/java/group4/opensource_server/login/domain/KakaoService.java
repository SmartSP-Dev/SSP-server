package group4.opensource_server.login.domain;

import group4.opensource_server.jwt.JwtUtil;
import group4.opensource_server.login.dto.KakaoTokenResponseDto;
import group4.opensource_server.login.dto.KakaoUserInfoResponseDto;
import group4.opensource_server.login.dto.LoginResponseDto;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Getter
public class KakaoService {
    @Value("${kakao.api_key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginResponseDto kakaoLogin(String code) {

        String accessTokenFromKakao = getAccessToken(code);

         KakaoUserInfoResponseDto kakaoUserInfoResponseDto = getKakaoUserInfo(accessTokenFromKakao);

        User user = userService.getUserByEmail(kakaoUserInfoResponseDto.getKakaoAccount().getEmail()).orElseGet(() -> {
                    return userService.createUser(User.builder()
                            .profileImage(kakaoUserInfoResponseDto.getKakaoAccount().getProfile().getThumbnailImageUrl())
                            .email(kakaoUserInfoResponseDto.getKakaoAccount().getEmail())
                            .provider("kakao")
                            .build());
                });

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public String getAccessToken(String code) {
        KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create("https://kauth.kakao.com").post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", kakaoApiKey)
                        .queryParam("redirect_uri", kakaoRedirectUri)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE,	"application/x-www-form-urlencoded")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();
        return kakaoTokenResponseDto.getAccessToken();
    }

    public KakaoUserInfoResponseDto getKakaoUserInfo(String accessToken) {
        KakaoUserInfoResponseDto userInfo = WebClient.create("https://kapi.kakao.com")
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, 	"application/x-www-form-urlencoded")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        return userInfo;
    }
}
