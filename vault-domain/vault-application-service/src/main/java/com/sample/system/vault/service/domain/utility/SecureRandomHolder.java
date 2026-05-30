package com.sample.system.vault.service.domain.utility;

import java.security.SecureRandom;

public final class SecureRandomHolder {
    private static final SecureRandom INSTANCE = new SecureRandom();

    private SecureRandomHolder() {
        // private constructor to prevent instantiation
    }

    public static int nextInt(int bound) {
        return INSTANCE.nextInt(bound);
    }
}
