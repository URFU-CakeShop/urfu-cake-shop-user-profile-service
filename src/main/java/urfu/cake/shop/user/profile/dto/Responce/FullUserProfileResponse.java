package com.example.urfu.cake.shop.user.profile.dto.Responce;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;


@Value
public class FullUserProfileResponse {
    private String firstName;

    private String lastName;

    private String phone;

    private List<AddressResponse> addressResponses;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
