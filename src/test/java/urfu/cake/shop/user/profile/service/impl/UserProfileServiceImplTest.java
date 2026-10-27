package urfu.cake.shop.user.profile.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import urfu.cake.shop.user.profile.dto.Request.CreateUserRequest;
import urfu.cake.shop.user.profile.dto.Request.UpdateUserRequest;
import urfu.cake.shop.user.profile.dto.Responce.AddressResponse;
import urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import urfu.cake.shop.user.profile.dto.Responce.UserResponse;
import urfu.cake.shop.user.profile.entity.AddressesEntity;
import urfu.cake.shop.user.profile.entity.UserProfilesEntity;
import urfu.cake.shop.user.profile.exception.UserNotFoundException;
import urfu.cake.shop.user.profile.exception.UserProfileAlreadyExistsException;
import urfu.cake.shop.user.profile.metrics.UserProfileMetrics;
import urfu.cake.shop.user.profile.repository.UserAddressesRepository;
import urfu.cake.shop.user.profile.repository.UserProfileRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserAddressesRepository userAddressesRepository;

    private UserProfileServiceImpl userProfileService;

    private UUID testUserId;
    private UserProfilesEntity testUserProfile;
    private AddressesEntity testAddress;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileServiceImpl(userProfileRepository, userAddressesRepository,
                new UserProfileMetrics(new SimpleMeterRegistry()));

        testUserId = UUID.randomUUID();
        testUserProfile = UserProfilesEntity.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .addresses(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testAddress = new AddressesEntity();
        testAddress.setId(UUID.randomUUID());
        testAddress.setUserProfiles(testUserProfile);
        testAddress.setCity("New York");
        testAddress.setStreet("5th Avenue");
        testAddress.setHouse("123");
        testAddress.setApartment("45");
        testAddress.setPostalCode("10001");
        testAddress.setDefault(true);
        testAddress.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getUserProfile returns user when found")
    void getUserProfile_userExists_returnsUser() {
        when(userProfileRepository.findOnlyUserByUserId(testUserId)).thenReturn(testUserProfile);

        ApiResponse<UserResponse> response = userProfileService.getUserProfile(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("John");
        assertThat(response.getData().getLastName()).isEqualTo("Doe");
        assertThat(response.getData().getPhone()).isEqualTo("+1234567890");
        assertThat(response.getMassage()).isNull();
        verify(userProfileRepository).findOnlyUserByUserId(testUserId);
    }

    @Test
    @DisplayName("getUserProfile throws UserNotFoundException when user not found")
    void getUserProfile_userNotFound_throwsException() {
        when(userProfileRepository.findOnlyUserByUserId(testUserId)).thenReturn(null);

        assertThatThrownBy(() -> userProfileService.getUserProfile(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + testUserId);

        verify(userProfileRepository).findOnlyUserByUserId(testUserId);
    }

    @Test
    @DisplayName("getUserAddress returns addresses list when found")
    void getUserAddress_addressesExist_returnsAddressesList() {
        List<AddressesEntity> addresses = List.of(testAddress);
        when(userAddressesRepository.findAllByUserProfiles_UserId(testUserId)).thenReturn(addresses);

        ApiResponse<List<AddressResponse>> response = userProfileService.getUserAddress(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCity()).isEqualTo("New York");
        assertThat(response.getData().get(0).getStreet()).isEqualTo("5th Avenue");
        assertThat(response.getData().get(0).getHouse()).isEqualTo("123");
        assertThat(response.getData().get(0).getApartment()).isEqualTo("45");
        assertThat(response.getData().get(0).getPostalCode()).isEqualTo("10001");
        assertThat(response.getData().get(0).isDefault()).isTrue();
        assertThat(response.getMassage()).isNull();
        verify(userAddressesRepository).findAllByUserProfiles_UserId(testUserId);
    }

    @Test
    @DisplayName("getUserAddress returns empty list when no addresses found")
    void getUserAddress_noAddresses_returnsEmptyList() {
        when(userAddressesRepository.findAllByUserProfiles_UserId(testUserId)).thenReturn(List.of());

        ApiResponse<List<AddressResponse>> response = userProfileService.getUserAddress(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
        assertThat(response.getMassage()).isNull();
        verify(userAddressesRepository).findAllByUserProfiles_UserId(testUserId);
    }

    @Test
    @DisplayName("getUserAddress returns empty list when null returned from repository")
    void getUserAddress_nullFromRepository_returnsEmptyList() {
        when(userAddressesRepository.findAllByUserProfiles_UserId(testUserId)).thenReturn(null);

        ApiResponse<List<AddressResponse>> response = userProfileService.getUserAddress(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
        verify(userAddressesRepository).findAllByUserProfiles_UserId(testUserId);
    }

    @Test
    @DisplayName("getFullUserProfile returns user with addresses when found")
    void getFullUserProfile_userExists_returnsFullProfile() {
        testUserProfile.getAddresses().add(testAddress);
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(testUserProfile);

        ApiResponse<FullUserProfileResponse> response = userProfileService.getFullUserProfile(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("John");
        assertThat(response.getData().getLastName()).isEqualTo("Doe");
        assertThat(response.getData().getPhone()).isEqualTo("+1234567890");
        assertThat(response.getData().getAddressResponses()).hasSize(1);
        assertThat(response.getData().getAddressResponses().get(0).getCity()).isEqualTo("New York");
        assertThat(response.getMassage()).isNull();
        verify(userProfileRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("getFullUserProfile throws UserNotFoundException when user not found")
    void getFullUserProfile_userNotFound_throwsException() {
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(null);

        assertThatThrownBy(() -> userProfileService.getFullUserProfile(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + testUserId);

        verify(userProfileRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("createUserProfile creates and returns profile")
    void createUserProfile_newUser_createsAndReturnsProfile() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhone("+9876543210");

        when(userProfileRepository.findByUserId(testUserId)).thenReturn(null);
        when(userProfileRepository.save(any(UserProfilesEntity.class))).thenAnswer(invocation -> {
            UserProfilesEntity saved = invocation.getArgument(0);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        ApiResponse<FullUserProfileResponse> response = userProfileService.createUserProfile(testUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("Jane");
        assertThat(response.getData().getLastName()).isEqualTo("Smith");
        assertThat(response.getData().getPhone()).isEqualTo("+9876543210");
        assertThat(response.getData().getAddressResponses()).isEmpty();
        assertThat(response.getMassage()).isNull();
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(any(UserProfilesEntity.class));
    }

    @Test
    @DisplayName("createUserProfile throws UserProfileAlreadyExistsException when duplicate")
    void createUserProfile_duplicateUser_throwsException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhone("+9876543210");

        when(userProfileRepository.findByUserId(testUserId)).thenReturn(testUserProfile);

        assertThatThrownBy(() -> userProfileService.createUserProfile(testUserId, request))
                .isInstanceOf(UserProfileAlreadyExistsException.class)
                .hasMessageContaining("Profile already exists for user: " + testUserId);

        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository, never()).save(any(UserProfilesEntity.class));
    }

    @Test
    @DisplayName("updateUserProfile updates and returns profile")
    void updateUserProfile_existingUser_updatesAndReturnsProfile() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John Updated");
        request.setLastName("Doe Updated");
        request.setPhone("+1111111111");

        when(userProfileRepository.findByUserId(testUserId)).thenReturn(testUserProfile);
        when(userProfileRepository.save(any(UserProfilesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<FullUserProfileResponse> response = userProfileService.updateUserProfile(testUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getFirstName()).isEqualTo("John Updated");
        assertThat(response.getData().getLastName()).isEqualTo("Doe Updated");
        assertThat(response.getData().getPhone()).isEqualTo("+1111111111");
        assertThat(response.getMassage()).isNull();
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("updateUserProfile throws UserNotFoundException when user not found")
    void updateUserProfile_userNotFound_throwsException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John Updated");
        request.setLastName("Doe Updated");
        request.setPhone("+1111111111");

        when(userProfileRepository.findByUserId(testUserId)).thenReturn(null);

        assertThatThrownBy(() -> userProfileService.updateUserProfile(testUserId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + testUserId);

        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository, never()).save(any(UserProfilesEntity.class));
    }

    @Test
    @DisplayName("deleteUserProfile deletes and returns true")
    void deleteUserProfile_existingUser_deletesAndReturnsTrue() {
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(testUserProfile);
        doNothing().when(userProfileRepository).delete(testUserProfile);

        ApiResponse<Boolean> response = userProfileService.deleteUserProfile(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isTrue();
        assertThat(response.getMassage()).isNull();
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).delete(testUserProfile);
    }

    @Test
    @DisplayName("deleteUserProfile throws UserNotFoundException when user not found")
    void deleteUserProfile_userNotFound_throwsException() {
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(null);

        assertThatThrownBy(() -> userProfileService.deleteUserProfile(testUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + testUserId);

        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository, never()).delete(any(UserProfilesEntity.class));
    }
}
