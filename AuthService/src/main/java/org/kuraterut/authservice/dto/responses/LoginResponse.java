package org.kuraterut.authservice.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Login Response")
public class LoginResponse {
    @Schema(description = "User access token")
    private String token;
}
