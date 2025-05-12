package group4.opensource_server.group.dto;

import group4.opensource_server.group.domain.TimeBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class TimeBlockDto {
    private List<TimeBlock> timeBlocks; // 240개

    public void addTimeBlocks(TimeBlock timeBlock) {
        timeBlocks.add(timeBlock);

        return ;
    }
}
