package group4.opensource_server.group.service;

import group4.opensource_server.group.domain.Group;
import group4.opensource_server.group.domain.GroupMembers;
import group4.opensource_server.group.dto.SimpleGroupDto;
import group4.opensource_server.group.repository.GroupMembersRepository;
import group4.opensource_server.group.repository.GroupRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Setter
@Getter
public class UserGroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembersRepository groupMembersRepository;

    public List<SimpleGroupDto> getUserGroup(String userId) {
        /* 임시 코드
        List<SimpleGroupDto> userGroups = List.of(
                new SimpleGroupDto(1, "스터디 그룹", "abcd1234"),
                new SimpleGroupDto(2, "팀 프로젝트", "efgh5678")
        );

         */

        int id = Integer.parseInt(userId);

        List<SimpleGroupDto> userGroups = new ArrayList<>();
        List<GroupMembers> groupMembers = groupMembersRepository.findByMemberId(id);

        for (GroupMembers i : groupMembers) {
            int groupId = i.getGroupId();
            Optional<Group> groupOptional = groupRepository.findById(groupId);

            if (groupOptional.isPresent()) {
                Group group = groupOptional.get();

                SimpleGroupDto dto = new SimpleGroupDto(group.getGroupId(), group.getGroupName(), group.getGroupKey());
                userGroups.add(dto);
            }

        }

        return userGroups;
    }

}
