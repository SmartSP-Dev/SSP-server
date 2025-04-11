package group4.opensource_server.user.dto;

import lombok.Getter;

@Getter
public class UserUpdateRequestDto {
    private String name;
    private String university;
    private String department;
    private String profileImage;
    private String everytimeUrl;
}
