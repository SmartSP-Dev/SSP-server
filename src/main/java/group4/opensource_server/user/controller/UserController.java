package group4.opensource_server.user.controller;

import group4.opensource_server.user.domain.UserService;
import group4.opensource_server.user.dto.UserDeleteResponseDto;
import group4.opensource_server.user.dto.UserResponseDto;
import group4.opensource_server.user.dto.UserUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 정보 조회 및 수정, 탈퇴 기능을 제공하는 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 반환합니다.")
    @GetMapping("/me")
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return UserResponseDto.from(
                userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다.")));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 이름, 프로필 이미지 등을 수정합니다.")
    @PatchMapping("/me")
    public UserResponseDto updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserUpdateRequestDto userUpdateRequestDto){
        return UserResponseDto.from(
                userService.updateUser(userDetails.getUsername(), userUpdateRequestDto));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    @DeleteMapping("/me")
    public UserDeleteResponseDto deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        return UserDeleteResponseDto.from(
                userService.deleteUser(userDetails.getUsername()));
    }
}