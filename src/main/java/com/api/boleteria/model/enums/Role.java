package com.api.boleteria.model.enums;

public enum Role {
    ADMIN,
    CLIENT;

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
