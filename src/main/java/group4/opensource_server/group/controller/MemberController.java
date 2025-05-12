package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.TimeBlockDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;

@RestController
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/when2meet/groups/{group_key}/members")
    public SuccessResponseDto enterGroup(@PathVariable("group_key") String groupKey, @RequestBody UserInfoDto user) {
        SuccessResponseDto responseDto = memberService.enterGroup(groupKey, user);

        return responseDto;
    }

    @PostMapping("/when2meet/groups/{group_key}/timetable")
    public SuccessResponseDto registerTimeTable(@PathVariable("group_key") String groupKey, @RequestBody TimeBlockDto timeTable, @RequestParam("userId") int userId) {
        SuccessResponseDto responseDto = memberService.registerTimeTable(groupKey, timeTable, userId);

        return responseDto;
    }

    @PutMapping("/when2meet/groups/{group_key}/timetable")
    public SuccessResponseDto updateTimeTable(@PathVariable("group_key") String groupKey, @RequestBody TimeBlockDto timeTable, @RequestParam("userId") int userId) {
        SuccessResponseDto responseDto = memberService.updateTimeTable(groupKey, timeTable, userId);

        return responseDto;
    }
}
