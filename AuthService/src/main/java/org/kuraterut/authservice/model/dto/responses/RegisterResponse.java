package org.kuraterut.authservice.model.dto.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RegisterResponse {
    private String token;
}
