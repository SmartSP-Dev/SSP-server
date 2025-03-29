package group4.opensource_server.login.controller;

import group4.opensource_server.login.domain.KakaoService;
import group4.opensource_server.login.dto.LoginResponseDto;
import group4.opensource_server.user.domain.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserService userService;

    @ResponseBody
    @GetMapping("/login/kakao")
    public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestParam String code){
        try{
            return ResponseEntity.ok(kakaoService.kakaoLogin(code));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Item Not Found");
        }
    }

}
