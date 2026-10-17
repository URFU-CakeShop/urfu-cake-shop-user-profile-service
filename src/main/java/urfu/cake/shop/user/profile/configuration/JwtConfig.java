package urfu.cake.shop.user.profile.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(String secret) {

    public JwtConfig {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret must be configured (env var JWT_SECRET)");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes (256 bits) for HS256");
        }
    }
}