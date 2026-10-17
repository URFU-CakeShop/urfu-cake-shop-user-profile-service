package urfu.cake.shop.user.profile.service;


import urfu.cake.shop.user.profile.dto.Request.CreateUserRequest;
import urfu.cake.shop.user.profile.dto.Request.UpdateUserRequest;
import urfu.cake.shop.user.profile.dto.Responce.AddressResponse;
import urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import urfu.cake.shop.user.profile.dto.Responce.UserResponse;

import java.util.List;
import java.util.UUID;


public interface UserProfilesService {

    ApiResponse<UserResponse> getUserProfile(UUID userUUID);

    ApiResponse<List<AddressResponse>> getUserAddress(UUID userUUID);

    ApiResponse<FullUserProfileResponse> getFullUserProfile(UUID userUUID);

    ApiResponse<FullUserProfileResponse> updateUserProfile(UUID userUUID, UpdateUserRequest body);

    ApiResponse<FullUserProfileResponse> createUserProfile(UUID userUUID, CreateUserRequest body);

    ApiResponse<Boolean> deleteUserProfile(UUID userUUID);

}
