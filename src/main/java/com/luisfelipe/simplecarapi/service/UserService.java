package com.luisfelipe.simplecarapi.service;

import com.luisfelipe.simplecarapi.domain.User;
import com.luisfelipe.simplecarapi.exception.UsernameAlreadyExistsException;
import com.luisfelipe.simplecarapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public User save(User user){
        if (repository.findByUsername(user.getUsername()).isPresent()){
            throw new UsernameAlreadyExistsException("Username " + user.getUsername() + " is already being used.");
        }
        return repository.save(user);
    }
}
