package group4.opensource_server.login.domain;

import group4.opensource_server.jwt.JwtUtil;
import group4.opensource_server.login.dto.AppleTokenResponseDto;
import group4.opensource_server.login.dto.LoginResponseDto;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleService {

    private final AppleClient appleClient;
    private final AppleJwtValidator appleJwtValidator;
    private final AppleJwtParser appleJwtParser;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public LoginResponseDto appleLogin(String authorizationCode) {
        AppleTokenResponseDto tokenResponse = appleClient.requestToken(authorizationCode);

        if (!appleJwtValidator.validate(tokenResponse.getIdToken())) {
            throw new RuntimeException("Invalid id_token");
        }

        AppleUser appleUser = appleJwtParser.parse(tokenResponse.getIdToken());

        User user = userService.getUserByEmail(appleUser.getEmail()).orElseGet(() -> {
            return userService.createUser(User.builder()
                    .email(appleUser.getEmail())
                    .provider("apple")
                    .build());
        });

        String newAccessToken = jwtUtil.generateAccessToken(appleUser.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(appleUser.getEmail());

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
