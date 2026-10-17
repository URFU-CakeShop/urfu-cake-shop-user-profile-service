package urfu.cake.shop.user.profile.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import urfu.cake.shop.user.profile.dto.Request.CreateUserRequest;
import urfu.cake.shop.user.profile.dto.Request.UpdateUserRequest;
import urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import urfu.cake.shop.user.profile.dto.Responce.UserResponse;
import urfu.cake.shop.user.profile.exception.UserNotFoundException;
import urfu.cake.shop.user.profile.exception.UserProfileAlreadyExistsException;
import urfu.cake.shop.user.profile.repository.UserProfileRepository;
import urfu.cake.shop.user.profile.service.UserProfilesService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class UserProfileServiceImplIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserProfilesService userProfilesService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        userProfileRepository.deleteAll();
    }

    @Test
    @DisplayName("Create user profile via service and verify persistence")
    void createUserProfile_validRequest_persistsSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setPhone("+1234567890");

        ApiResponse<FullUserProfileResponse> response = userProfilesService.createUserProfile(testUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("Alice");
        assertThat(response.getData().getLastName()).isEqualTo("Johnson");
        assertThat(response.getData().getPhone()).isEqualTo("+1234567890");
        assertThat(response.getData().getAddressResponses()).isEmpty();
    }

    @Test
    @DisplayName("Get user profile after creation returns correct data")
    void getUserProfile_afterCreation_returnsCorrectData() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Bob");
        request.setLastName("Smith");
        request.setPhone("+9876543210");

        userProfilesService.createUserProfile(testUserId, request);

        ApiResponse<UserResponse> response = userProfilesService.getUserProfile(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("Bob");
        assertThat(response.getData().getLastName()).isEqualTo("Smith");
        assertThat(response.getData().getPhone()).isEqualTo("+9876543210");
    }

    @Test
    @DisplayName("Get non-existent user throws UserNotFoundException")
    void getUserProfile_nonExistentUser_throwsException() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThatThrownBy(() -> userProfilesService.getUserProfile(nonExistentUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + nonExistentUserId);
    }

    @Test
    @DisplayName("Create duplicate user profile throws UserProfileAlreadyExistsException")
    void createUserProfile_duplicateUser_throwsException() {
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setFirstName("Charlie");
        request1.setLastName("Brown");
        request1.setPhone("+1111111111");

        userProfilesService.createUserProfile(testUserId, request1);

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setFirstName("Charlie");
        request2.setLastName("Brown Updated");
        request2.setPhone("+2222222222");

        assertThatThrownBy(() -> userProfilesService.createUserProfile(testUserId, request2))
                .isInstanceOf(UserProfileAlreadyExistsException.class)
                .hasMessageContaining("Profile already exists for user: " + testUserId);
    }

    @Test
    @DisplayName("Update user profile persists changes correctly")
    void updateUserProfile_existingUser_updatesSuccessfully() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setFirstName("David");
        createRequest.setLastName("Wilson");
        createRequest.setPhone("+3333333333");

        userProfilesService.createUserProfile(testUserId, createRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("David Updated");
        updateRequest.setLastName("Wilson Updated");
        updateRequest.setPhone("+4444444444");

        ApiResponse<FullUserProfileResponse> response = userProfilesService.updateUserProfile(testUserId, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("David Updated");
        assertThat(response.getData().getLastName()).isEqualTo("Wilson Updated");
        assertThat(response.getData().getPhone()).isEqualTo("+4444444444");

        ApiResponse<UserResponse> verifyResponse = userProfilesService.getUserProfile(testUserId);
        assertThat(verifyResponse.getData().getFirstName()).isEqualTo("David Updated");
        assertThat(verifyResponse.getData().getLastName()).isEqualTo("Wilson Updated");
        assertThat(verifyResponse.getData().getPhone()).isEqualTo("+4444444444");
    }

    @Test
    @DisplayName("Update non-existent user throws UserNotFoundException")
    void updateUserProfile_nonExistentUser_throwsException() {
        UUID nonExistentUserId = UUID.randomUUID();

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Nobody");
        updateRequest.setLastName("Nowhere");
        updateRequest.setPhone("+0000000000");

        assertThatThrownBy(() -> userProfilesService.updateUserProfile(nonExistentUserId, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + nonExistentUserId);
    }

    @Test
    @DisplayName("Delete user profile removes user from database")
    void deleteUserProfile_existingUser_removesSuccessfully() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setFirstName("Eve");
        createRequest.setLastName("Martinez");
        createRequest.setPhone("+5555555555");

        userProfilesService.createUserProfile(testUserId, createRequest);

        ApiResponse<Boolean> deleteResponse = userProfilesService.deleteUserProfile(testUserId);

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.isSuccess()).isTrue();
        assertThat(deleteResponse.getData()).isTrue();

        assertThatThrownBy(() -> userProfilesService.getUserProfile(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + testUserId);
    }

    @Test
    @DisplayName("Delete non-existent user throws UserNotFoundException")
    void deleteUserProfile_nonExistentUser_throwsException() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThatThrownBy(() -> userProfilesService.deleteUserProfile(nonExistentUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + nonExistentUserId);
    }

    @Test
    @DisplayName("Get full user profile returns user with empty addresses")
    void getFullUserProfile_userWithNoAddresses_returnsProfileWithEmptyAddressList() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setFirstName("Frank");
        createRequest.setLastName("Davis");
        createRequest.setPhone("+6666666666");

        userProfilesService.createUserProfile(testUserId, createRequest);

        ApiResponse<FullUserProfileResponse> response = userProfilesService.getFullUserProfile(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("Frank");
        assertThat(response.getData().getLastName()).isEqualTo("Davis");
        assertThat(response.getData().getPhone()).isEqualTo("+6666666666");
        assertThat(response.getData().getAddressResponses()).isEmpty();
    }

    @Test
    @DisplayName("Get addresses for user with no addresses returns empty list")
    void getUserAddress_userWithNoAddresses_returnsEmptyList() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setFirstName("Grace");
        createRequest.setLastName("Lee");
        createRequest.setPhone("+7777777777");

        userProfilesService.createUserProfile(testUserId, createRequest);

        var response = userProfilesService.getUserAddress(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
    }
}
