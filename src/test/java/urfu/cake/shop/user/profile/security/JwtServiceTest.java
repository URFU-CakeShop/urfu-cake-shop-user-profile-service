package urfu.cake.shop.user.profile.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import urfu.cake.shop.user.profile.configuration.JwtConfig;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-0123456789-0123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtConfig(SECRET));
    }

    @Test
    @DisplayName("isValidToken returns true for a valid token")
    void isValidToken_validToken_returnsTrue() {
        UUID userId = UUID.randomUUID();
        String token = JwtTestUtils.createToken(SECRET, userId);

        boolean result = jwtService.isValidToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValidToken returns false for an invalid token")
    void isValidToken_invalidToken_returnsFalse() {
        boolean result = jwtService.isValidToken("invalid.token.value");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValidToken returns false for token signed with different key")
    void isValidToken_wrongKey_returnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = JwtTestUtils.createToken("different-secret-key-0123456789-different!", userId);

        boolean result = jwtService.isValidToken(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValidToken returns false for expired token")
    void isValidToken_expiredToken_returnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = JwtTestUtils.createToken(SECRET, userId, -3_600_000L);

        boolean result = jwtService.isValidToken(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("extractUserUuid returns correct UUID for a valid token")
    void extractUserUuid_validToken_returnsUuid() {
        UUID userId = UUID.randomUUID();
        String token = JwtTestUtils.createToken(SECRET, userId);

        String extracted = jwtService.extractUserUuid(token);

        assertThat(extracted).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("extractUserUuid returns null for an invalid token")
    void extractUserUuid_invalidToken_returnsNull() {
        String extracted = jwtService.extractUserUuid("garbage.token.value");

        assertThat(extracted).isNull();
    }

    @Test
    @DisplayName("extractUserUuid returns null for expired token")
    void extractUserUuid_expiredToken_returnsNull() {
        UUID userId = UUID.randomUUID();
        String token = JwtTestUtils.createToken(SECRET, userId, -3_600_000L);

        String extracted = jwtService.extractUserUuid(token);

        assertThat(extracted).isNull();
    }

    @Test
    @DisplayName("extractUserUuid returns null for null token")
    void extractUserUuid_nullToken_returnsNull() {
        String extracted = jwtService.extractUserUuid(null);

        assertThat(extracted).isNull();
    }
}