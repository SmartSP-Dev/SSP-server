package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.SimpleGroupDto;
import group4.opensource_server.group.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserGroupController {
    @Autowired
    private UserGroupService userService;

    @GetMapping("/when2meet/users/{user_id}/groups")
    public List<SimpleGroupDto> getUserGroups(@PathVariable("user_id") String userId) {
        List<SimpleGroupDto> userGroups = userService.getUserGroup(userId);

        return userGroups;
    }
}
