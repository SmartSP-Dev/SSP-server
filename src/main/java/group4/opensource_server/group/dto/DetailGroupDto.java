package group4.opensource_server.group.dto;

import group4.opensource_server.group.domain.TimeBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetailGroupDto {
    private int groupId;
    private String groupName;
    private String groupKey;
    private int leaderId;
    private String startDate;
    private String endDate;
    private String expiresAt;
    private List<TimeBlock> timeBlocks;
    private int memberCount;
    private List<Integer> memberIds;

}
