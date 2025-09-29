package com.clinicboard.user_service.infrastructure.adapters.out.feign;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "business-service")
public interface BusinessFeignClient {

    @GetMapping(path = "/patients/user/{userId}")
    List<Optional<?>> findByUserId(@PathVariable("userId") String userId);

}
