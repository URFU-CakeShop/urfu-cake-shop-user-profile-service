package com.example.urfu.cake.shop.user.profile.service.impl;


import com.example.urfu.cake.shop.user.profile.dto.Request.UUIDRequest;
import com.example.urfu.cake.shop.user.profile.dto.Responce.AddressResponse;
import com.example.urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import com.example.urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import com.example.urfu.cake.shop.user.profile.dto.Responce.UserResponse;
import com.example.urfu.cake.shop.user.profile.entity.AddressesEntity;
import com.example.urfu.cake.shop.user.profile.entity.UserProfilesEntity;
import com.example.urfu.cake.shop.user.profile.repository.UserAddressesRepository;
import com.example.urfu.cake.shop.user.profile.repository.UserProfileRepository;
import com.example.urfu.cake.shop.user.profile.service.UserProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfilesService {

    private final UserProfileRepository userProfileRepository;
    private final UserAddressesRepository userAddressesRepository;

    public ApiResponse<UserResponse> getUserProfile(UUIDRequest request) {
        UserProfilesEntity userProfiles = userProfileRepository.findOnlyUserByUserId(request.getUserUUID());

        return userProfiles == null ?
                new ApiResponse<>(false, null, "Пользователь не найден"):
                new ApiResponse<>(true, toModel(userProfiles), "Нашлось");

    }

    public ApiResponse<List<AddressResponse>> getUserAddress(UUIDRequest request){
        List<AddressesEntity> entity = userAddressesRepository.findAllByUserProfiles_UserId(request.getUserUUID());

        return (entity == null || entity.isEmpty()) ?
                new ApiResponse<>(false, null, "Адреса еще не существует") :
                new ApiResponse<>(true, entity.stream()
                                                .map(this::toModel)
                                                .toList(), "Нашлось");
    }

    public ApiResponse<FullUserProfileResponse> getFullUserProfile(UUIDRequest request){
        UserProfilesEntity userProfiles = userProfileRepository.findByUserId(request.getUserUUID());

        return userProfiles == null  ?
                new ApiResponse<>(false, null, "Пользователь не найден"):
                new ApiResponse<>(true, toFullModel(userProfiles), "Нашлось");
    }



    private UserResponse toModel(UserProfilesEntity profile){

        return new UserResponse(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private AddressResponse toModel(AddressesEntity addresses){

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
    private FullUserProfileResponse toFullModel(UserProfilesEntity profile){

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
