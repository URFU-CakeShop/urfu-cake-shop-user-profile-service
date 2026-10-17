package com.example.urfu.cake.shop.user.profile.repository;


import com.example.urfu.cake.shop.user.profile.entity.UserProfilesEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfilesEntity, UUID> {

    UserProfilesEntity findOnlyUserByUserId(UUID userId);

    @EntityGraph(attributePaths = {"addresses"})
    UserProfilesEntity findByUserId(UUID userId);

}
