package org.kuraterut.authservice.model.dto.requests;

import jakarta.validation.constraints.Email;
import lombok.*;
import org.kuraterut.authservice.model.Role;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RegisterRequest {
    @Email
    private String email;
    private String password;
    private Role role;
}
