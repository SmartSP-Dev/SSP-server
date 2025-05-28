package group4.opensource_server.group.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuccessResponseDto {
    private boolean successed;

    public SuccessResponseDto(boolean successed) {
        this.successed = successed;
    }
}
