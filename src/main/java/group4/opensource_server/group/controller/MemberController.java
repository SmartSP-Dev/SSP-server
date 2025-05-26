package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.TimeBlockDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.service.MemberService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
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

    @PostMapping("/when2meet/groups/{group_key}/timetable")
    public SuccessResponseDto registerTimeTable(@PathVariable("group_key") String groupKey, @RequestBody TimeBlockDto timeTable, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();

        SuccessResponseDto responseDto = memberService.registerTimeTable(groupKey, timeTable, userId);

        return responseDto;
    }

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
