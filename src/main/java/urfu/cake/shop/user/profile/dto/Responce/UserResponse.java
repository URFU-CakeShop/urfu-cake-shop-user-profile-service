package urfu.cake.shop.user.profile.dto.Responce;

import lombok.Value;

import java.time.LocalDateTime;


@Value
public class UserResponse {

    private String firstName;

    private String lastName;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
