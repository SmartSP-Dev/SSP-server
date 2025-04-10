package group4.opensource_server.login.controller;

import com.fasterxml.jackson.databind.JsonNode;
import group4.opensource_server.login.domain.AppleService;
import group4.opensource_server.login.domain.KakaoService;
import group4.opensource_server.login.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class LoginController {
    private final KakaoService kakaoService;
    private final AppleService appleService;

    @ResponseBody
    @GetMapping("/login/kakao")
    public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestParam String code){
        try{
            return ResponseEntity.ok(kakaoService.kakaoLogin(code));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Item Not Found");
        }
    }

    @GetMapping("/login/apple/callback")
    public ResponseEntity<?> appleCallback(@RequestParam("code") String code,
                                           @RequestParam(value = "state", required = false) String state) {
        try {
            // Apple 토큰 교환 진행
            JsonNode tokenResponse = appleService.exchangeCodeForTokens(code);
            String idToken = tokenResponse.get("id_token").asText();

            // id_token 검증 및 사용자 정보 추출
            Claims claims = appleLoginService.verifyIdToken(idToken);
            String appleUserId = claims.getSubject();
            String email = (String) claims.get("email");

            // 기존 JWT 기반 인증 로직과 연계해 사용자 로그인/등록 처리
            // 예를 들어, 사용자 DB 검색 후 신규 사용자인 경우 등록하고 JWT 발급

            return ResponseEntity.ok("Apple 로그인 성공, 사용자 ID: " + appleUserId + ", email: " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Apple 로그인 실패: " + e.getMessage());
        }
    }

}
