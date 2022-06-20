package com.nosqldb.controller.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Data
public class DBUser implements UserDetails {

    private String database;
    private String role;
    private String username;
    private String password;

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authority = new ArrayList<>();
        authority.add((GrantedAuthority) () -> "ROLE_USER");
        return authority;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DBUser)) {
            return false;
        }
        DBUser user = (DBUser) o;
        return user.getUsername().equals(getUsername()) &&
                user.getDatabase().equals(getDatabase());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + database.hashCode();
        result = 31 * result + username.hashCode();
        return result;
    }

}
