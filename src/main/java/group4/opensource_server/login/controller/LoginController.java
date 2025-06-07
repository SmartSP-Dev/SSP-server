package group4.opensource_server.login.controller;

import group4.opensource_server.login.domain.AppleService;
import group4.opensource_server.login.domain.KakaoService;
import group4.opensource_server.login.dto.LoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;


@Tag(name = "Login", description = "카카오/애플 로그인 및 JWT 발급을 처리하는 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class LoginController {
    private final KakaoService kakaoService;
    private final AppleService appleService;

    @Operation(summary = "카카오 로그인 처리- web", description = "프론트엔드에서 전달받은 카카오 인가 코드를 사용해 JWT 토큰을 발급합니다.")
    @PostMapping("/login/kakao")
    public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestBody Map<String, String> body){
        try{
            return ResponseEntity.ok(kakaoService.kakaoLogin(body.get("code")));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Item Not Found");
        }
    }
    @Operation(summary = "카카오 로그인 처리- ios", description = "프론트엔드에서 전달받은 카카오 인가 코드를 사용해 JWT 토큰을 발급합니다.")
    @PostMapping("/login/kakao/token")
    public ResponseEntity<LoginResponseDto> loginWithAccessToken(@RequestBody Map<String, String> body) {
        LoginResponseDto response = kakaoService.kakaoLoginWithToken(body.get("accessToken"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "애플 로그인 처리", description = "프론트엔드에서 전달받은 애플 인가 코드를 사용해 JWT 토큰을 발급합니다.")
    @PostMapping("/login/apple")
    public ResponseEntity<LoginResponseDto> appleLogin(@RequestBody Map<String, String> body) {
        try{
            return ResponseEntity.ok(appleService.appleLogin(body.get("code")));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Item Not Found");
        }
    }
}
