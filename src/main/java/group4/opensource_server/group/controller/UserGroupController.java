package group4.opensource_server.group.controller;

import group4.opensource_server.group.dto.SimpleGroupDto;
import group4.opensource_server.group.service.UserGroupService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserGroupController {
    @Autowired
    private UserGroupService userService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "가입 그룹 가져오기", description = "사용자가 가입한 웬투밋 그룹의 간단한 정보를 가져옵니다.")
    @GetMapping("/when2meet/users/groups")
    public List<SimpleGroupDto> getUserGroups(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        int userId = currentUser.getId();

        List<SimpleGroupDto> userGroups = userService.getUserGroup(userId);

        return userGroups;
    }
}
