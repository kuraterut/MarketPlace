package org.kuraterut.authservice.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Login request")
public class LoginRequest {
    @Email(message = "Login email must be valid")
    @Schema(description = "User email", example = "example@test.com")
    private String email;

    @NotBlank(message = "Login Password must be not empty")
    @Schema(description = "User password", example = "qwertyytrewq")
    private String password;
}
