package group4.opensource_server.group.service;

import group4.opensource_server.group.domain.Group;
import group4.opensource_server.group.domain.GroupMembers;
import group4.opensource_server.group.domain.TimeTables;
import group4.opensource_server.group.dto.GroupKeyDto;
import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.repository.GroupMembersRepository;
import group4.opensource_server.group.repository.GroupRepository;
import group4.opensource_server.group.repository.TimeTablesRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Service
@Getter
@Setter
@AllArgsConstructor
@Transactional
public class LeaderService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembersRepository groupMembersRepository;

    @Autowired
    private TimeTablesRepository timeTablesRepository;

    public SuccessResponseDto deleteGroup(String groupKey, UserInfoDto user) {
        // 테스트코드
        // SuccessResponseDto responseDto = new SuccessResponseDto(false);

        SuccessResponseDto responseDto;
        int userId = user.getId();

        Optional<Group> tempGroup = groupRepository.findByGroupKey(groupKey);
        Group group;

        if (tempGroup.isPresent()) {
            group = tempGroup.get();
        }
        else {
            throw new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다.");
        }

        int leaderId = group.getLeaderId();
        int groupId = group.getGroupId();

        if (userId == leaderId) {
            groupRepository.deleteByGroupKey(groupKey);
            timeTablesRepository.deleteByGroupId(groupId);
            groupMembersRepository.deleteByGroupId(groupId);

            responseDto = new SuccessResponseDto(true);
        }
        else {
            responseDto = new SuccessResponseDto(false);
        }

        return responseDto;
    }

    public SuccessResponseDto changeLeader(String groupKey, UserInfoDto prevLeader, UserInfoDto newLeader) {
        SuccessResponseDto responseDto;

        Optional<Group> tempGroup = groupRepository.findByGroupKey(groupKey);
        Group group;

        if (tempGroup.isPresent()) {
            group = tempGroup.get();
        }
        else {
            throw new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다.");
        }

        List<GroupMembers> groupMembersList = groupMembersRepository.findByGroupId(group.getGroupId());
        GroupMembers groupMember = new GroupMembers(group.getGroupId(), newLeader.getId());
        boolean isMember = false;
        for (GroupMembers member : groupMembersList) {
            if (member.getMemberId() == newLeader.getId()) {
                isMember = true;
                break;
            }
        }


        if (!isMember) {
            throw new RuntimeException("기존 멤버에게만 방장을 넘길 수 있습니다.");
        }


        int leaderId = group.getLeaderId();

        if (prevLeader.getId() == leaderId) {
            int result = groupRepository.updateLeaderByGroupKey(groupKey, newLeader.getId());

            if (result == 0) {
                throw new RuntimeException("리더 변경 실패: 해당 그룹이 존재하지 않거나 이미 변경됨.");
            }

            responseDto = new SuccessResponseDto(true);
        }
        else {
            responseDto = new SuccessResponseDto(false); // 리더가 아닌데 넘기려는 경우
        }

        return responseDto;

    }

    public GroupKeyDto getKey(String groupKey, UserInfoDto user) {
        GroupKeyDto responseDto;
        int userId = user.getId();

        Optional<Group> tempGroup = groupRepository.findByGroupKey(groupKey);
        Group group;

        if (tempGroup.isPresent()) {
            group = tempGroup.get();
        }
        else {
            throw new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다.");
        }

        int leaderId = group.getLeaderId();

        if (userId == leaderId) {
            responseDto = new GroupKeyDto(groupKey, true);
        }
        else {
            responseDto = new GroupKeyDto(null,false);
        }

        return responseDto;
    }
}
