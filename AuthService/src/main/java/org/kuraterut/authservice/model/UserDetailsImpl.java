package org.kuraterut.authservice.model;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
public class UserDetailsImpl implements UserDetails {
    private final Collection<? extends GrantedAuthority> authorities;
    private final String email;
    private final String password;
    @Getter
    private final Long userId;


    public UserDetailsImpl(String email, String password, Long userId, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

}