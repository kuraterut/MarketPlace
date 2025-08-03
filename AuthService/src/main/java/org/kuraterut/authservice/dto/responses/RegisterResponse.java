package org.kuraterut.authservice.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Registration Response")
public class RegisterResponse {
    @Schema(description = "User access token")
    private String token;
}
