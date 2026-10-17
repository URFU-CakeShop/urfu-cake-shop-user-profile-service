package urfu.cake.shop.user.profile.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(java.util.UUID userId) {
        super("User not found: " + userId);
    }
}