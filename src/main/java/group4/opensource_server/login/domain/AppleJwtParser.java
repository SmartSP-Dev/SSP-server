package group4.opensource_server.login.domain;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

@Component
public class AppleJwtParser {

    public AppleUser parse(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            return new AppleUser(
                    claims.getSubject(),
                    claims.getStringClaim("email"),
                    claims.getBooleanClaim("email_verified")
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid Apple ID Token", e);
        }
    }
}