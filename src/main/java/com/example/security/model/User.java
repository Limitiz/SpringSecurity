package com.example.security.model;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="USERS")
@SequenceGenerator(name="USERS_SEQ_GENERATOR",
        sequenceName="SEQ_USERS",
        initialValue=1,
        allocationSize=1)
@Entity
public class User implements UserDetails{

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="USERS_SEQ_GENERATOR")
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(length=300, nullable=false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getRole()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}

