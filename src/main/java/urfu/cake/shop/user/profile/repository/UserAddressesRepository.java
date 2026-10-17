package com.example.urfu.cake.shop.user.profile.repository;


import com.example.urfu.cake.shop.user.profile.entity.AddressesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAddressesRepository extends JpaRepository<AddressesEntity, UUID> {

    // Создание запросов из имени метода. В UserProfiles найди UserId
    List<AddressesEntity> findAllByUserProfiles_UserId(UUID userId);
}
