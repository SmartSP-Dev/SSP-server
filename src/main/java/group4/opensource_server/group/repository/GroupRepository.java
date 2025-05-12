package group4.opensource_server.group.repository;

import group4.opensource_server.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByGroupId(int groupId);

    Optional<Group> findByGroupKey(String groupKey);

    void deleteByGroupKey(String groupKey);

    @Modifying
    @Query("UPDATE Group g SET g.leaderId = :newLeaderId WHERE g.groupKey = :groupKey")
    int updateLeaderByGroupKey(@Param("groupKey") String groupKey, @Param("newLeaderId") int newLeaderId);

}
