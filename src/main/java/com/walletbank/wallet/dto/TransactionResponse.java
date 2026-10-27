package com.walletbank.wallet.dto;

import com.walletbank.wallet.entity.Transaction;
import com.walletbank.wallet.entity.TransactionStatus;
import com.walletbank.wallet.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        String fromAccount,
        String toAccount,
        String description,
        BigDecimal balanceAfter,
        LocalDateTime createdAt) {

    public static TransactionResponse from(Transaction tx, BigDecimal balanceAfter) {
        return new TransactionResponse(
                tx.getId(),
                tx.getType(),
                tx.getAmount(),
                tx.getStatus(),
                tx.getFromWallet() != null ? tx.getFromWallet().getAccountNumber() : null,
                tx.getToWallet() != null ? tx.getToWallet().getAccountNumber() : null,
                tx.getDescription(),
                balanceAfter,
                tx.getCreatedAt());
    }
}
