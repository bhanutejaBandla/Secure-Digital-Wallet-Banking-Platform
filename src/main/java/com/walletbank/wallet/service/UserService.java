package com.walletbank.wallet.service;

import com.walletbank.wallet.dto.RegisterRequest;
import com.walletbank.wallet.dto.UserResponse;
import com.walletbank.wallet.entity.Role;
import com.walletbank.wallet.entity.User;
import com.walletbank.wallet.exception.EmailAlreadyExistsException;
import com.walletbank.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        return UserResponse.from(userRepository.save(user));
    }
}
