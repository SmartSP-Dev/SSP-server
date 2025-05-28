package group4.opensource_server.group.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_members")
@IdClass(GroupMembersId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembers {
    @Id
    @Column(name = "group_id", nullable = false)
    private int groupId;

    @Id
    @Column(name = "member_id", nullable = false)
    private int memberId;
}
