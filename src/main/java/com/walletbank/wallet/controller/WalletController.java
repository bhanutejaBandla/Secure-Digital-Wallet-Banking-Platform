package com.walletbank.wallet.controller;

import com.walletbank.wallet.dto.MoneyRequest;
import com.walletbank.wallet.dto.TransactionResponse;
import com.walletbank.wallet.dto.TransferRequest;
import com.walletbank.wallet.dto.WalletResponse;
import com.walletbank.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(principal.getUsername()));
    }

    @GetMapping("/me")
    public WalletResponse myWallet(@AuthenticationPrincipal UserDetails principal) {
        return walletService.getMyWallet(principal.getUsername());
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(@AuthenticationPrincipal UserDetails principal,
                                       @Valid @RequestBody MoneyRequest request) {
        return walletService.deposit(principal.getUsername(), request);
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(@AuthenticationPrincipal UserDetails principal,
                                        @Valid @RequestBody MoneyRequest request) {
        return walletService.withdraw(principal.getUsername(), request);
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(@AuthenticationPrincipal UserDetails principal,
                                        @Valid @RequestBody TransferRequest request) {
        return walletService.transfer(principal.getUsername(), request);
    }
}
