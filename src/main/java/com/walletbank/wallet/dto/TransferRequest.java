package com.walletbank.wallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Recipient account number is required")
        @Pattern(regexp = "\\d{12}", message = "Account number must be exactly 12 digits")
        String toAccountNumber,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @DecimalMax(value = "10000000.00", message = "Amount exceeds the per-transaction limit")
        @Digits(integer = 8, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description) {
}
