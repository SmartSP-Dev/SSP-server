package group4.opensource_server.login.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppleUser {
    private String sub;
    private String email;
    private boolean emailVerified;
}
