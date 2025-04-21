package com.flight.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userservice")
public interface UserClient {

    @GetMapping("/users/{userId}")
    UserResponse getUserById(@PathVariable Long userId);
}
