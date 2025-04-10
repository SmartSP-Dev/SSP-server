package group4.opensource_server.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public class AppleJwtUtil {
    // 환경 변수로 전달된 .p8 PEM 문자열을 ECPrivateKey로 변환
    public static ECPrivateKey loadPrivateKeyFromString(String keyContent) throws Exception {
        String privateKeyPEM = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPrivateKey) kf.generatePrivate(keySpec);
    }

    // 클라이언트 시크릿 JWT 생성 메서드
    public static String generateClientSecret(
            String teamId,
            String keyId,
            String clientId,
            String privateKeyContent
    ) throws Exception {
        ECPrivateKey privateKey = loadPrivateKeyFromString(privateKeyContent);
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        // 예시로 30일 후 만료 (필요시 수정)
        Date expiration = Date.from(now.plus(30, ChronoUnit.DAYS));

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(teamId)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }
}
