package org.kuraterut.authservice.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.kuraterut.authservice.model.utils.Role;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Registration request")
public class RegisterRequest {
    @Email(message = "Register email must be valid")
    @Schema(description = "User email", example = "example@test.com")
    private String email;

    @NotBlank(message = "Register password must be not empty")
    @Size(min = 8, max = 16, message = "Password length must be from 8 to 16 symbs")
    @Schema(description = "User password", example = "qwertyytrewq")
    private String password;

    @NotNull(message = "User Role must be not null")
    @Schema(description = "User role", example = "ADMIN")
    private Role role;
}
