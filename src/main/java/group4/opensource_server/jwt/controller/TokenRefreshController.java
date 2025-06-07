package group4.opensource_server.jwt.controller;
import group4.opensource_server.jwt.JwtUtil;
import group4.opensource_server.jwt.dto.TokenRefreshRequestDto;
import group4.opensource_server.jwt.dto.TokenRefreshResponseDto;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "액세스 토큰 재발급",
            description = "리프레시 토큰을 이용해 새로운 액세스 토큰을 발급받습니다. 유효한 리프레시 토큰이 필요합니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refreshTokens(
            @RequestBody TokenRefreshRequestDto request) {

        String oldRefreshToken = request.getRefreshToken();
        String newAccessToken = jwtUtil.refreshAccessToken(oldRefreshToken);

        String subject         = jwtUtil.extractUsername(oldRefreshToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(subject);

        return ResponseEntity.ok(TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build());
    }

}