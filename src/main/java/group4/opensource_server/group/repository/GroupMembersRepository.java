package group4.opensource_server.group.repository;

import group4.opensource_server.group.domain.GroupMembersId;
import group4.opensource_server.group.domain.GroupMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMembersRepository extends JpaRepository<GroupMembers, GroupMembersId> {
    List<GroupMembers> findByGroupId(int groupId);

    List<GroupMembers> findByMemberId(int memberId);

    Optional<GroupMembers> findByGroupIdAndMemberId(int groupId, int memberId);

    boolean existsByGroupIdAndMemberId(int groupId, int userId);

    void deleteByGroupId(int groupId);
}
