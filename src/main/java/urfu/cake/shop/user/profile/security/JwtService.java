package urfu.cake.shop.user.profile.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import urfu.cake.shop.user.profile.configuration.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    public boolean isValidToken(String token) {
        try {
            parser().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserUuid(String token) {
        try {
            Claims claims = parser().parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT extraction failed: {}", e.getMessage());
            return null;
        }
    }

    private JwtParser parser() {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtConfig.secret().getBytes(StandardCharsets.UTF_8));
    }
}