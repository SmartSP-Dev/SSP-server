package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.*;
import group4.opensource_server.group.service.GroupService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {
    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    // 그룹을 만드는 유저의 토큰을 받음
    // leaderId는 null로 받으면 좋을듯
    @Operation(summary = "그룹 생성", description = "웬투밋 그룹을 생성합니다.")
    @PostMapping("/when2meet/groups")
    public SimpleGroupDto createGroup(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CreateGroupRequestDto requestDto) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();

        requestDto.setLeaderId(userId);

        SimpleGroupDto responseDto = groupService.createGroup(requestDto);

        return responseDto;
    }

    @Operation(summary = "멤버 구하기", description = "웬투밋 그룹의 멤버를 가져옵니다.")
    @GetMapping("/when2meet/groups/{group_key}/members")
    public List<UserInfoDto> getGroupMember(@PathVariable("group_key") String groupKey) {
        List<UserInfoDto> responseDto = groupService.getGroupMember(groupKey);

        return responseDto;
    }

    @Operation(summary = "웬투밋 시간표 가져오기", description = "웬투밋 그룹의 등록된 시간과 가중치를 가져옵니다.")
    @GetMapping("/when2meet/groups/{group_key}/timetable")
    public TimeBlockDto getTimeBlockAndWeight(@PathVariable("group_key") String groupKey) {
        TimeBlockDto responseDto = groupService.getTimeBlockAndWeight(groupKey);

        return responseDto;
    }

    @Operation(summary = "시간 블록 정보 가져오기", description = "시간 블록의 가중치와 선택한 멤버를 가져옵니다.")
    @PostMapping("/when2meet/groups/{group_key}/timetable/weightAndMembers")
    public WeightAndMembers getWeightAndMembers(@PathVariable("group_key") String groupKey, @RequestBody DayAndTime requestDto) {
        WeightAndMembers responseDto = groupService.getWeightAndMembers(groupKey, requestDto);

        return responseDto;
    }

}
