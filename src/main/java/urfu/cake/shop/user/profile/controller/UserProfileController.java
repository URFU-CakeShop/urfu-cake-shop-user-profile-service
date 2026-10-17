package urfu.cake.shop.user.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import urfu.cake.shop.user.profile.dto.Request.CreateUserRequest;
import urfu.cake.shop.user.profile.dto.Request.UpdateUserRequest;
import urfu.cake.shop.user.profile.dto.Responce.AddressResponse;
import urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import urfu.cake.shop.user.profile.dto.Responce.UserResponse;
import urfu.cake.shop.user.profile.service.UserProfilesService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfilesService userProfile;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getUser(Authentication authentication) {
        return ResponseEntity.ok(userProfile.getUserProfile(currentUserId(authentication)));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(Authentication authentication) {
        return ResponseEntity.ok(userProfile.getUserAddress(currentUserId(authentication)));
    }

    @GetMapping("/full")
    public ResponseEntity<ApiResponse<FullUserProfileResponse>> getFullUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfile.getFullUserProfile(currentUserId(authentication)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<FullUserProfileResponse>> createUser(
            Authentication authentication,
            @Valid @RequestBody CreateUserRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfile.createUserProfile(currentUserId(authentication), body));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<FullUserProfileResponse>> updateUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest body) {
        return ResponseEntity.ok(userProfile.updateUserProfile(currentUserId(authentication), body));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(Authentication authentication) {
        return ResponseEntity.ok(userProfile.deleteUserProfile(currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("User identity missing in token");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user identity format in token: " + authentication.getName());
        }
    }
}