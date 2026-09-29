package com.walletbank.wallet.repository;

import com.walletbank.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromWalletIdOrToWalletId(Long fromId, Long toId, Pageable pageable);
}
