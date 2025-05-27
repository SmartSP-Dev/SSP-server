package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.TimeBlockDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.service.MemberService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;

@RestController
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "그룹 참여하기", description = "그룹키에 해당하는 웬투밋 그룹에 참여합니다.")
    @PostMapping("/when2meet/groups/{group_key}/members")
    public SuccessResponseDto enterGroup(@PathVariable("group_key") String groupKey, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();
        String userName = currentUser.getName();

        UserInfoDto user = new UserInfoDto(userName, userId);

        SuccessResponseDto responseDto = memberService.enterGroup(groupKey, user);

        return responseDto;
    }

    @Operation(summary = "웬투밋 시간 등록하기", description = "신규 멤버의 일정을 등록합니다.")
    @PostMapping("/when2meet/groups/{group_key}/timetable")
    public SuccessResponseDto registerTimeTable(@PathVariable("group_key") String groupKey, @RequestBody TimeBlockDto timeTable, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();

        SuccessResponseDto responseDto = memberService.registerTimeTable(groupKey, timeTable, userId);

        return responseDto;
    }

    @Operation(summary = "웬투밋 시간 수정하기", description = "기존 멤버의 웬투밋 등록 일정을 수정합니다.")
    @PutMapping("/when2meet/groups/{group_key}/timetable")
    public SuccessResponseDto updateTimeTable(@PathVariable("group_key") String groupKey, @RequestBody TimeBlockDto timeTable, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();
        SuccessResponseDto responseDto = memberService.updateTimeTable(groupKey, timeTable, userId);

        return responseDto;
    }
}
