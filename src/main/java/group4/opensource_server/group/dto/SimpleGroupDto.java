package group4.opensource_server.group.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SimpleGroupDto {
    private int groupId;
    private String groupName;
    private String groupKey;
}
