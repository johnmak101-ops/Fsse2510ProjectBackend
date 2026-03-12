package com.fsse2510.fsse2510_project_backend.repository;

import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {

        @EntityGraph(attributePaths = {"user"})
        @Query("SELECT t FROM TransactionEntity t")
        Page<TransactionEntity> findAllWithItems(Pageable pageable);

        Optional<TransactionEntity> findByTidAndUser(Integer tid, UserEntity user);

        Optional<TransactionEntity> findByTidAndUser_Uid(Integer tid, Integer uid);

        @Query("SELECT DISTINCT t FROM TransactionEntity t LEFT JOIN FETCH t.items WHERE t.user.uid = :uid ORDER BY t.datetime DESC")
        List<TransactionEntity> findAllByUser_Uid(@Param("uid") Integer uid);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT t FROM TransactionEntity t WHERE t.tid = :tid AND t.user.uid = :uid")
        Optional<TransactionEntity> findByTidAndUser_UidWithLock(
                        @Param("tid") Integer tid,
                        @Param("uid") Integer uid);

        @Query("SELECT SUM(tp.quantity) FROM TransactionEntity t JOIN t.items tp WHERE tp.sku = :sku AND t.status IN :statuses")
        Long sumPendingQuantityBySku(@Param("sku") String sku, @Param("statuses") Collection<PaymentStatus> statuses);

        @Query("SELECT tp.sku, SUM(tp.quantity) FROM TransactionEntity t JOIN t.items tp WHERE tp.sku IN :skus AND t.status IN :statuses GROUP BY tp.sku")
        List<Object[]> sumPendingQuantityBySkuIn(@Param("skus") Collection<String> skus, @Param("statuses") Collection<PaymentStatus> statuses);

        @Query("SELECT SUM(t.usedPoints) FROM TransactionEntity t WHERE t.user.uid = :uid AND t.status IN :statuses")
        Long sumPendingPointsByUser(@Param("uid") Integer uid, @Param("statuses") Collection<PaymentStatus> statuses);

        List<TransactionEntity> findAllByStatusAndDatetimeBefore(PaymentStatus status, LocalDateTime cutoff);
}