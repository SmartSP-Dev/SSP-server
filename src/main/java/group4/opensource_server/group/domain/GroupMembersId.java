package group4.opensource_server.group.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupMembersId implements Serializable {
    private int groupId;
    private int memberId;
}

