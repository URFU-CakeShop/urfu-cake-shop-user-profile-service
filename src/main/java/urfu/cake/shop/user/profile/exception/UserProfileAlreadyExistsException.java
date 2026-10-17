package urfu.cake.shop.user.profile.exception;

public class UserProfileAlreadyExistsException extends RuntimeException {
    public UserProfileAlreadyExistsException(java.util.UUID userId) {
        super("Profile already exists for user: " + userId);
    }
}