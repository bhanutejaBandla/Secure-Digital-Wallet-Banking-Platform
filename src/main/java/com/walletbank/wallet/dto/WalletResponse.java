package com.walletbank.wallet.dto;

import com.walletbank.wallet.entity.Wallet;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String ownerName,
        LocalDateTime createdAt) {

    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getAccountNumber(),
                wallet.getBalance(),
                wallet.getUser().getFullName(),
                wallet.getCreatedAt());
    }
}
