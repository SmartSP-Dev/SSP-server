package group4.opensource_server.login.domain;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.*;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AppleJwtValidator {

    @Value("${apple.team_id}")
    private String teamId;

    @Value("${apple.client_id}")
    private String clientId;

    @Value("${apple.key_id}")
    private String keyId;

    @Value("${apple.private_key}")
    private String privateKey;

    public boolean validate(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWKSet jwkSet = JWKSet.load(new java.net.URL("https://appleid.apple.com/auth/keys"));
            JWK jwk = jwkSet.getKeyByKeyId(jwt.getHeader().getKeyID());
            RSAKey rsaKey = (RSAKey) jwk;
            return jwt.verify(new RSASSAVerifier(rsaKey));
        } catch (Exception e) {
            return false;
        }
    }

    public String generateClientSecret() {
        try {
            ECPrivateKey privateKey = getPrivateKey();

            JWSSigner signer = new ECDSASigner(privateKey);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(new java.util.Date())
                    .expirationTime(new java.util.Date(System.currentTimeMillis() + 300000))
                    .audience("https://appleid.apple.com")
                    .subject(clientId)
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(keyId)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);

            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate client secret", e);
        }
    }

    private ECPrivateKey getPrivateKey() throws Exception {
        String pem = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
