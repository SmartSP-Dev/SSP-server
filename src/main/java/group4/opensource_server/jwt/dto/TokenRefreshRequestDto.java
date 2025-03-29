package group4.opensource_server.jwt.dto;

import lombok.Data;

@Data
public class TokenRefreshRequestDto {
    private String refreshToken;
}