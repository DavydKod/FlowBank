package com.davyd.security.authorization;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    public void requireOwner(long currentUserId, long ownerId) {
        if (currentUserId != ownerId) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
