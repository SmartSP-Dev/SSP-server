package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.GroupKeyDto;
import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.service.LeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeaderController {
    @Autowired
    private LeaderService leaderService;

    @DeleteMapping("/when2meet/groups/{group_key}")
    public SuccessResponseDto deleteGroup(@PathVariable("group_key") String groupKey, @RequestBody UserInfoDto user) {
        SuccessResponseDto responseDto = leaderService.deleteGroup(groupKey, user);

        return responseDto;
    }

    @PutMapping("/when2meet/groups/{group_key}/group_leader")
    public SuccessResponseDto changeLeader(@PathVariable("group_key") String groupKey, @RequestBody UserInfoDto[] users) {
        UserInfoDto prevLeader = users[0];
        UserInfoDto newLeader = users[1];

        SuccessResponseDto responseDto = leaderService.changeLeader(groupKey, prevLeader, newLeader);

        return responseDto;
    }

    @GetMapping("/when2meet/groups/{group_key}/group_key")
    public GroupKeyDto getKey(@PathVariable("group_key") String groupKey, @RequestBody UserInfoDto user) {
        GroupKeyDto response = leaderService.getKey(groupKey, user);

        return response;
    }

}
