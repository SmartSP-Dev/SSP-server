package group4.opensource_server.user.dto;

import group4.opensource_server.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Integer userId;
    private String email;
    private String name;
    private String university;
    private String department;
    private String profileImage;
    private String provider;
    private String everytimeUrl;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .university(user.getUniversity())
                .department(user.getDepartment())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .everytimeUrl(user.getEverytimeUrl())
                .build();
    }
}