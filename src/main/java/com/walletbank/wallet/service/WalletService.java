package com.walletbank.wallet.service;

import com.walletbank.wallet.dto.WalletResponse;
import com.walletbank.wallet.entity.User;
import com.walletbank.wallet.entity.Wallet;
import com.walletbank.wallet.exception.ResourceNotFoundException;
import com.walletbank.wallet.exception.WalletAlreadyExistsException;
import com.walletbank.wallet.repository.UserRepository;
import com.walletbank.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Transactional
    public WalletResponse createWallet(String email) {
        User user = findUser(email);

        if (walletRepository.findByUserId(user.getId()).isPresent()) {
            throw new WalletAlreadyExistsException("Wallet already exists for this user");
        }

        Wallet wallet = Wallet.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        return WalletResponse.from(walletRepository.save(wallet));
    }

    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(String email) {
        User user = findUser(email);

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found. Create one first with POST /api/wallets"));

        return WalletResponse.from(wallet);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private String generateAccountNumber() {
        String number;
        do {
            long n = RANDOM.nextLong(100_000_000_000L, 1_000_000_000_000L); // 12 digits
            number = String.valueOf(n);
        } while (walletRepository.existsByAccountNumber(number));
        return number;
    }
}
