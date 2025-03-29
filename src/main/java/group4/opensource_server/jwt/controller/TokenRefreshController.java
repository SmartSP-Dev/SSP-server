package group4.opensource_server.jwt.controller;
import group4.opensource_server.jwt.JwtUtil;
import group4.opensource_server.jwt.dto.TokenRefreshRequestDto;
import group4.opensource_server.jwt.dto.TokenRefreshResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenRefreshController {

    private final JwtUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refreshAccessToken(@RequestBody TokenRefreshRequestDto request) {
        String newAccessToken = jwtUtil.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .build());
    }
}