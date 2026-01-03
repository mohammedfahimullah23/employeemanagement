package com.example.learningjava.service;

import com.example.learningjava.model.User;
import com.example.learningjava.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findOrCreateOauthUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setPasswordHash(null);
                    String username = email.substring(0, email.indexOf("@"));
                    user.setUsername(username);
                    return userRepository.save(user);
                });
    }
}
