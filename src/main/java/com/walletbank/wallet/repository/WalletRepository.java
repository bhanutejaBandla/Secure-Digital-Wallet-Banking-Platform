package com.walletbank.wallet.repository;

import com.walletbank.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);
    Optional<Wallet> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);

    // Scalar queries: return only the id, so no Wallet entity is loaded into the
    // persistence context before we lock it (avoids reading a stale balance).
    @Query("select w.id from Wallet w where w.user.id = :userId")
    Optional<Long> findIdByUserId(@Param("userId") Long userId);

    @Query("select w.id from Wallet w where w.accountNumber = :accountNumber")
    Optional<Long> findIdByAccountNumber(@Param("accountNumber") String accountNumber);
}
