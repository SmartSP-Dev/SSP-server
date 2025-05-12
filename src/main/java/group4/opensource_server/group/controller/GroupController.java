package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.CreateGroupRequestDto;
import group4.opensource_server.group.dto.SimpleGroupDto;
import group4.opensource_server.group.dto.TimeBlockDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {
    @Autowired
    private GroupService groupService;

    @PostMapping("/when2meet/groups")
    public SimpleGroupDto createGroup(@RequestBody CreateGroupRequestDto requestDto) {
        SimpleGroupDto responseDto = groupService.createGroup(requestDto);

        return responseDto;
    }

    @GetMapping("/when2meet/groups/{group_key}/members")
    public List<UserInfoDto> getGroupMember(@PathVariable("group_key") String groupKey) {
        List<UserInfoDto> responseDto = groupService.getGroupMember(groupKey);

        return responseDto;
    }

    @GetMapping("/when2meet/groups/{group_key}/timetable")
    public TimeBlockDto getTimeBlockWeight(@PathVariable("group_key") String groupKey) {
        TimeBlockDto responseDto = groupService.getTimeBlockWeight(groupKey);

        return responseDto;
    }

}
