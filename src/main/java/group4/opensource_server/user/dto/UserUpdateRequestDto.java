package group4.opensource_server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserUpdateRequestDto {
    private String name;
    private String university;
    private String department;
    private String profileImage;
    private String everytimeUrl;
}
