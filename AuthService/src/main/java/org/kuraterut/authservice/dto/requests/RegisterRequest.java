package org.kuraterut.authservice.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.kuraterut.authservice.model.Role;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RegisterRequest {
    @Email(message = "Register email must be valid")
    private String email;

    @NotBlank(message = "Register password must be not empty")
    @Size(min = 8, max = 16, message = "Password length must be from 8 to 16 symbs")
    private String password;

    @NotNull(message = "User Role must be not null")
    private Role role;
}
