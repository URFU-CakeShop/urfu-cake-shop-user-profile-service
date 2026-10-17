package com.example.urfu.cake.shop.user.profile.service;


import com.example.urfu.cake.shop.user.profile.dto.Request.UUIDRequest;
import com.example.urfu.cake.shop.user.profile.dto.Responce.AddressResponse;
import com.example.urfu.cake.shop.user.profile.dto.Responce.ApiResponse;
import com.example.urfu.cake.shop.user.profile.dto.Responce.FullUserProfileResponse;
import com.example.urfu.cake.shop.user.profile.dto.Responce.UserResponse;

import java.util.List;


public interface UserProfilesService {

    ApiResponse<UserResponse> getUserProfile(UUIDRequest request);

    ApiResponse<List<AddressResponse>> getUserAddress(UUIDRequest request);

    ApiResponse<FullUserProfileResponse> getFullUserProfile(UUIDRequest request);

}
