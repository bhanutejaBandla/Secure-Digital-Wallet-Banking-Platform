package com.walletbank.wallet.service;

import com.walletbank.wallet.dto.MoneyRequest;
import com.walletbank.wallet.dto.TransactionResponse;
import com.walletbank.wallet.dto.TransferRequest;
import com.walletbank.wallet.dto.WalletResponse;
import com.walletbank.wallet.entity.Transaction;
import com.walletbank.wallet.entity.TransactionStatus;
import com.walletbank.wallet.entity.TransactionType;
import com.walletbank.wallet.entity.User;
import com.walletbank.wallet.entity.Wallet;
import com.walletbank.wallet.exception.InsufficientBalanceException;
import com.walletbank.wallet.exception.InvalidTransferException;
import com.walletbank.wallet.exception.ResourceNotFoundException;
import com.walletbank.wallet.exception.WalletAlreadyExistsException;
import com.walletbank.wallet.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;

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

    @Transactional
    public TransactionResponse deposit(String email, MoneyRequest request) {
        Wallet wallet = lockWallet(findUser(email));

        wallet.setBalance(wallet.getBalance().add(request.amount()));

        Transaction tx = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .toWallet(wallet)
                .description(descriptionOrDefault(request.description(), "Deposit"))
                .build();
        transactionRepository.save(tx);

        return TransactionResponse.from(tx, wallet.getBalance());
    }

    @Transactional
    public TransactionResponse withdraw(String email, MoneyRequest request) {
        Wallet wallet = lockWallet(findUser(email));

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));

        Transaction tx = Transaction.builder()
                .type(TransactionType.WITHDRAW)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .fromWallet(wallet)
                .description(descriptionOrDefault(request.description(), "Withdrawal"))
                .build();
        transactionRepository.save(tx);

        return TransactionResponse.from(tx, wallet.getBalance());
    }

    @Transactional
    public TransactionResponse transfer(String email, TransferRequest request) {
        User user = findUser(email);

        Long senderId = walletRepository.findIdByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found. Create one first with POST /api/wallets"));
        Long receiverId = walletRepository.findIdByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient account not found"));

        if (senderId.equals(receiverId)) {
            throw new InvalidTransferException("Cannot transfer to your own wallet");
        }

        // Always lock in ascending id order so two opposite transfers can never deadlock.
        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);
        Wallet first = lockById(firstId);
        Wallet second = lockById(secondId);

        Wallet sender = senderId.equals(firstId) ? first : second;
        Wallet receiver = senderId.equals(firstId) ? second : first;

        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));

        Transaction tx = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .fromWallet(sender)
                .toWallet(receiver)
                .description(descriptionOrDefault(request.description(), "Transfer"))
                .build();
        transactionRepository.save(tx);

        return TransactionResponse.from(tx, sender.getBalance());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Wallet lockWallet(User user) {
        return walletRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found. Create one first with POST /api/wallets"));
    }

    private Wallet lockById(Long walletId) {
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }

    private String descriptionOrDefault(String description, String fallback) {
        return (description == null || description.isBlank()) ? fallback : description.trim();
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
