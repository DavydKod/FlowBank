package com.davyd.security.authorization;

import com.davyd.common.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class OwnershipValidator {

    // using NotFoundException instead of AccessDeniedException to prevent users from knowing if the
    // resource under certain ID exists or not
    public void requireOwner(long currentUserId, long ownerId, Supplier<? extends NotFoundException> exception) {
        if (currentUserId != ownerId) {
            throw exception.get();
        }
    }
}
