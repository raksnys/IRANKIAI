package com.irankiai.backend.user;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    private static final Set<String> VALID_ROLES = Set.of("admin", "buyer", "merchant");

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            return user.getId() + ":" + user.getRole();
        }
        return "error: Invalid username or password";
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.get("role");

        // Validate role
        if (!VALID_ROLES.contains(role)) {
            return "error: Invalid role. Must be one of: admin, buyer, merchant";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return "error: Username already exists";
        }

        User newUser = new User(0, username, password, role);
        userRepository.save(newUser);
        return "success";
    }
} 