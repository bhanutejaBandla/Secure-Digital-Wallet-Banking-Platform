package com.walletbank.wallet.dto;

public record AuthResponse(String token, String tokenType, long expiresIn) {
}
