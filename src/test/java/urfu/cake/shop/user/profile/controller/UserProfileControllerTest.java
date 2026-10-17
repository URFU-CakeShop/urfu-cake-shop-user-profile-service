package urfu.cake.shop.user.profile.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import urfu.cake.shop.user.profile.dto.Request.CreateUserRequest;
import urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import urfu.cake.shop.user.profile.dto.Responce.UserResponse;
import urfu.cake.shop.user.profile.service.UserProfilesService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private UserProfilesService userProfileService;

    @InjectMocks
    private UserProfileController controller;

    @Test
    @DisplayName("currentUserId extracts UUID from authentication name")
    void getUser_authenticationProvided_extractsUuid() {
        UUID userId = UUID.randomUUID();
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of());

        when(userProfileService.getUserProfile(userId)).thenReturn(
                new ApiResponse<>(true, new UserResponse("John", "Doe", "+123",
                        LocalDateTime.now(), LocalDateTime.now()), null));

        var response = controller.getUser(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(userProfileService).getUserProfile(userId);
    }

    @Test
    @DisplayName("createUser extracts UUID from authentication and returns 201")
    void createUser_authenticationProvided_createsUser() {
        UUID userId = UUID.randomUUID();
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of());
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+123");

        when(userProfileService.createUserProfile(eq(userId), any(CreateUserRequest.class)))
                .thenReturn(new ApiResponse<>(true, new FullUserProfileResponse("John", "Doe", "+123",
                        List.of(), LocalDateTime.now(), LocalDateTime.now()), null));

        var response = controller.createUser(authentication, request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        verify(userProfileService).createUserProfile(eq(userId), any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("deleteUser extracts UUID from authentication")
    void deleteUser_authenticationProvided_deletesUser() {
        UUID userId = UUID.randomUUID();
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of());

        when(userProfileService.deleteUserProfile(userId))
                .thenReturn(new ApiResponse<>(true, true, null));

        var response = controller.deleteUser(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(userProfileService).deleteUserProfile(userId);
    }

    @Test
    @DisplayName("updateUser extracts UUID from authentication")
    void updateUser_authenticationProvided_updatesUser() {
        UUID userId = UUID.randomUUID();
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of());
        var request = new urfu.cake.shop.user.profile.dto.Request.UpdateUserRequest();
        request.setFirstName("John Updated");
        request.setLastName("Doe Updated");
        request.setPhone("+456");

        when(userProfileService.updateUserProfile(eq(userId), any()))
                .thenReturn(new ApiResponse<>(true, new FullUserProfileResponse("John Updated", "Doe Updated", "+456",
                        List.of(), LocalDateTime.now(), LocalDateTime.now()), null));

        var response = controller.updateUser(authentication, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(userProfileService).updateUserProfile(eq(userId), any());
    }
}