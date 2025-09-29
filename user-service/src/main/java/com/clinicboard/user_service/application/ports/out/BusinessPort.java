package com.clinicboard.user_service.application.ports.out;

import java.util.List;
import java.util.Optional;

public interface BusinessPort {
    List<Optional<?>> findByUserId(String userId);
}
