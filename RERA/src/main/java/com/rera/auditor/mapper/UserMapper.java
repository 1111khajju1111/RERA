package com.rera.auditor.mapper;

import com.rera.auditor.dto.UserResponse;
import com.rera.auditor.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
