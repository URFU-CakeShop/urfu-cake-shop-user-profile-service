package urfu.cake.shop.user.profile.service.impl;


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
import urfu.cake.shop.user.profile.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfilesService {

    private final UserProfileRepository userProfileRepository;
    private final UserAddressesRepository userAddressesRepository;
    private final UserProfileMetrics metrics;

    @Override
    public ApiResponse<UserResponse> getUserProfile(UUID userUUID) {
        return metrics.record("get", () -> {
            UserProfilesEntity userProfiles = userProfileRepository.findOnlyUserByUserId(userUUID);

            if (userProfiles == null) {
                throw new UserNotFoundException(userUUID);
            }

            return new ApiResponse<>(true, toModel(userProfiles), null);
        });
    }

    @Override
    public ApiResponse<List<AddressResponse>> getUserAddress(UUID userUUID) {
        return metrics.record("get_addresses", () -> {
            List<AddressesEntity> entity = userAddressesRepository.findAllByUserProfiles_UserId(userUUID);

            if (entity == null || entity.isEmpty()) {
                return new ApiResponse<>(true, List.of(), null);
            }

            return new ApiResponse<>(true, entity.stream()
                    .map(this::toModel)
                    .toList(), null);
        });
    }

    @Override
    public ApiResponse<FullUserProfileResponse> getFullUserProfile(UUID userUUID) {
        return metrics.record("get_full", () -> {
            UserProfilesEntity userProfiles = userProfileRepository.findByUserId(userUUID);

            if (userProfiles == null) {
                throw new UserNotFoundException(userUUID);
            }

            return new ApiResponse<>(true, toFullModel(userProfiles), null);
        });
    }

    @Override
    @Transactional
    public ApiResponse<FullUserProfileResponse> updateUserProfile(UUID userUUID, UpdateUserRequest body) {
        return metrics.record("update", () -> {
            UserProfilesEntity entity = userProfileRepository.findByUserId(userUUID);

            if (entity == null) {
                throw new UserNotFoundException(userUUID);
            }

            entity.setFirstName(body.getFirstName());
            entity.setLastName(body.getLastName());
            entity.setPhone(body.getPhone());

            userProfileRepository.save(entity);

            return new ApiResponse<>(true, toFullModel(entity), null);
        });
    }

    @Override
    @Transactional
    public ApiResponse<FullUserProfileResponse> createUserProfile(UUID userUUID, CreateUserRequest body) {
        return metrics.record("create", () -> {
            if (userProfileRepository.findByUserId(userUUID) != null) {
                throw new UserProfileAlreadyExistsException(userUUID);
            }

            UserProfilesEntity entity = UserProfilesEntity.builder()
                    .userId(userUUID)
                    .firstName(body.getFirstName())
                    .lastName(body.getLastName())
                    .phone(body.getPhone())
                    .build();

            userProfileRepository.save(entity);

            return new ApiResponse<>(true, toFullModel(entity), null);
        });
    }

    @Override
    @Transactional
    public ApiResponse<Boolean> deleteUserProfile(UUID userUUID) {
        return metrics.record("delete", () -> {
            UserProfilesEntity entity = userProfileRepository.findByUserId(userUUID);

            if (entity == null) {
                throw new UserNotFoundException(userUUID);
            }

            userProfileRepository.delete(entity);
            return new ApiResponse<>(true, true, null);
        });
    }


    private UserResponse toModel(UserProfilesEntity profile) {
        return new UserResponse(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private AddressResponse toModel(AddressesEntity addresses) {
        return new AddressResponse(
                addresses.getCity(),
                addresses.getStreet(),
                addresses.getHouse(),
                addresses.getApartment(),
                addresses.getPostalCode(),
                addresses.isDefault(),
                addresses.getCreatedAt()
        );
    }

    private FullUserProfileResponse toFullModel(UserProfilesEntity profile) {
        return new FullUserProfileResponse(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getAddresses().stream()
                        .map(this::toModel)
                        .toList(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
