package com.luisfelipe.simplecarapi.mapper;

import com.luisfelipe.simplecarapi.annotation.EncodedMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncoderMapper {
    private final PasswordEncoder passwordEncoder;

    @EncodedMapping
    public String encodedPassword(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }
}
