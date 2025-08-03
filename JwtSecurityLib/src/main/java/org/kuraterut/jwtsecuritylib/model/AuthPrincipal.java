package org.kuraterut.jwtsecuritylib.model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthPrincipal {
    private String email;
    private Long userId;
    private List<String> roles;
}
