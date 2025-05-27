package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.GroupKeyDto;
import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.service.LeaderService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeaderController {
    @Autowired
    private LeaderService leaderService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "그룹 삭제", description = "웬투밋 그룹을 삭제합니다.")
    @DeleteMapping("/when2meet/groups/{group_key}")
    public SuccessResponseDto deleteGroup(@PathVariable("group_key") String groupKey, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();
        String userName = currentUser.getName();

        UserInfoDto user = new UserInfoDto(userName, userId);

        SuccessResponseDto responseDto = leaderService.deleteGroup(groupKey, user);

        return responseDto;
    }

/*
    @PutMapping("/when2meet/groups/{group_key}/group_leader")
    public SuccessResponseDto changeLeader(@PathVariable("group_key") String groupKey, @RequestBody UserInfoDto[] users) {
        UserInfoDto prevLeader = users[0];
        UserInfoDto newLeader = users[1];

        SuccessResponseDto responseDto = leaderService.changeLeader(groupKey, prevLeader, newLeader);

        return responseDto;
    }
*/

    @Operation(summary = "그룹키 가져오기", description = "해당 웬투밋 그룹의 키를 가져옵니다")
    @GetMapping("/when2meet/groups/{group_key}/group_key")
    public GroupKeyDto getKey(@PathVariable("group_key") String groupKey, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();
        String userName = currentUser.getName();

        UserInfoDto user = new UserInfoDto(userName, userId);

        GroupKeyDto response = leaderService.getKey(groupKey, user);

        return response;
    }

}
