package group4.opensource_server.group.domain;

import group4.opensource_server.group.dto.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class TimeBlock {
    String dayOfWeek; // "MON", "TUE", ...
    LocalTime time; // "08:00", "05:30", ... "22:30". 하루에 총 30개? 일주일에 총 210개?
    int weight = 0;
    List<UserInfoDto> blockMembers = new LinkedList<>();

    public void addBlockMembers(UserInfoDto userInfoDto) {
        blockMembers.add(userInfoDto);

        return ;
    }
}
