package urfu.cake.shop.user.profile.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UUIDRequest {

    @NotNull
    private UUID userUUID;

}
