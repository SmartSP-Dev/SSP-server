package group4.opensource_server.user.controller;

import group4.opensource_server.user.domain.UserService;
import group4.opensource_server.user.dto.UserDeleteResponseDto;
import group4.opensource_server.user.dto.UserResponseDto;
import group4.opensource_server.user.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return UserResponseDto.from(
                userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다.")));
    }

    @PatchMapping("/me")
    public UserResponseDto updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails, UserUpdateRequestDto userUpdateRequestDto){
        return UserResponseDto.from(
                userService.updateUser(userDetails.getUsername(), userUpdateRequestDto));

    }

    @DeleteMapping("/me")
    public UserDeleteResponseDto deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        return UserDeleteResponseDto.from(
                userService.deleteUser(userDetails.getUsername()));
    }
}
