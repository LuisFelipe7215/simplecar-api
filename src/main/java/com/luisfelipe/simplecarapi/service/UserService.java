package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.User;
import com.luisfelipe.simplecarapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public User save(User user){
        return repository.save(user);
    }
}
