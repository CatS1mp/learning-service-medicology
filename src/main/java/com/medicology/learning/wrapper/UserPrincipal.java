package com.medicology.learning.wrapper;

import java.util.UUID;

import lombok.Getter;

@Getter
public class UserPrincipal {
    private final UUID id;
    private final String email;
    private final boolean admin;

    public UserPrincipal(UUID id, String email) {
        this(id, email, false);
    }

    public UserPrincipal(UUID id, String email, boolean admin) {
        this.id = id;
        this.email = email;
        this.admin = admin;
    }
}
