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
public class UserDeleteResponseDto {
    private Integer userId;
    private String email;

    public static UserDeleteResponseDto from(User user) {
        return UserDeleteResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail()).build();
    }
}
