package org.kuraterut.authservice.model.dto.requests;

import jakarta.validation.constraints.Email;
import lombok.*;
import org.kuraterut.authservice.model.Role;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequest {
    @Email
    private String email;
    private String password;
}
