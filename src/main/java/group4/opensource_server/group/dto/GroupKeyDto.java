package group4.opensource_server.group.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupKeyDto {
    private String groupKey;
    private boolean isLeader;
}
