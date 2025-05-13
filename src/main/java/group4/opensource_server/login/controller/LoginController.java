package group4.opensource_server.login.controller;

import group4.opensource_server.login.domain.AppleService;
import group4.opensource_server.login.domain.KakaoService;
import group4.opensource_server.login.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class LoginController {
    private final KakaoService kakaoService;
    private final AppleService appleService;

    @PostMapping("/login/kakao")
    public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestBody Map<String, String> body){
        try{
            return ResponseEntity.ok(kakaoService.kakaoLogin(body.get("code")));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Item Not Found");
        }
    }

    @PostMapping("/login/kakao/token")
    public ResponseEntity<LoginResponseDto> loginWithAccessToken(@RequestBody Map<String, String> body) {
        LoginResponseDto response = kakaoService.kakaoLoginWithToken(body.get("accessToken"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/apple")
    public ResponseEntity<LoginResponseDto> appleLogin(@RequestBody Map<String, String> body) {
        try{
            return ResponseEntity.ok(appleService.appleLogin(body.get("code")));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Item Not Found");
        }
    }
}
