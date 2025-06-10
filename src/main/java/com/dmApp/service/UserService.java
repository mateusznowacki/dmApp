package com.dmApp.service;


import com.dmApp.entity.User;
import com.dmApp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(PasswordService passwordService, UserRepository userRepository) {
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

    public boolean isValidUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {

            return passwordService.matches(password, user.get().getPassword());
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
       return userRepository.findByEmail(email);
    }
}