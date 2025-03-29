package group4.opensource_server.login.domain;

import group4.opensource_server.jwt.JwtUtil;
import group4.opensource_server.login.dto.KakaoTokenResponseDto;
import group4.opensource_server.login.dto.KakaoUserInfoResponseDto;
import group4.opensource_server.login.dto.LoginResponseDto;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import group4.opensource_server.user.domain.UserService;
import io.netty.handler.codec.http.HttpHeaderValues;
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
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponseDto kakaoLogin(String code) {

        // Access Token 요청
        String accessTokenFromKakao = getAccessToken(code);

        // Kakao 사용자 정보 요청
        KakaoUserInfoResponseDto kakaoUserInfoResponseDto = getKakaoUserInfo(accessTokenFromKakao);

        // 기존 사용자 조회 혹은 신규 회원가입
        User user = userRepository.findByEmail(kakaoUserInfoResponseDto.getKakaoAccount().getEmail())
                .orElseGet(() -> {
                    return userRepository.save(User.builder()
                            .profileImage(kakaoUserInfoResponseDto.getKakaoAccount().getProfile().getThumbnailImageUrl())
                            .email(kakaoUserInfoResponseDto.getKakaoAccount().getEmail())
                            .nickname(kakaoUserInfoResponseDto.getKakaoAccount().getProfile().getNickname())
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
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
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
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        return userInfo;
    }
}
