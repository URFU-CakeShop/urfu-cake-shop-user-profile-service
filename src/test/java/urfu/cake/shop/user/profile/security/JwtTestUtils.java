package urfu.cake.shop.user.profile.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public final class JwtTestUtils {

    private JwtTestUtils() {
    }

    public static String createToken(String secret, UUID subject) {
        return createToken(secret, subject, 3_600_000L);
    }

    public static String createToken(String secret, UUID subject, long ttlMillis) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject.toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(key)
                .compact();
    }
}