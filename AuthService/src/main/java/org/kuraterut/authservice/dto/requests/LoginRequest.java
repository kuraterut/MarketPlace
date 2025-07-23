package org.kuraterut.authservice.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.kuraterut.authservice.model.Role;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginRequest {
    @Email(message = "Login email must be valid")
    private String email;

    @NotBlank(message = "Login Password must be not empty")
    private String password;
}
