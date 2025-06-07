package group4.opensource_server.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
}