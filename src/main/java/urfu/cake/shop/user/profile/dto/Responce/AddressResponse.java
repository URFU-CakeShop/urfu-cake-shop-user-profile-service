package com.example.urfu.cake.shop.user.profile.dto.Responce;


import lombok.Value;

import java.time.LocalDateTime;

@Value
public class AddressResponse {

    private String city;


    private String street;


    private String house;


    private String apartment;


    private String postalCode;


    private boolean isDefault;


    private LocalDateTime createdAt;
}
