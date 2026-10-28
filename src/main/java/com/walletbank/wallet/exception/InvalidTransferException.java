package com.walletbank.wallet.exception;

public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
