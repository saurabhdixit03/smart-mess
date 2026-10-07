package com.smartmess.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.PaymentOrder;

import jakarta.persistence.LockModeType;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByPaymentOrderIdAndMess_MessId(
            Long paymentOrderId,
            Long messId
    );

    Optional<PaymentOrder> findByMess_MessIdAndActiveBillId(
            Long messId,
            Long activeBillId
    );

    List<PaymentOrder> findByMess_MessIdAndBill_BillIdOrderByCreatedAtDesc(
            Long messId,
            Long billId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT po
            FROM PaymentOrder po
            WHERE po.paymentOrderId = :paymentOrderId
              AND po.mess.messId = :messId
            """)
    Optional<PaymentOrder> findByIdAndMessIdForUpdate(
            @Param("paymentOrderId") Long paymentOrderId,
            @Param("messId") Long messId
    );

    /*
     * Trusted gateway processing only.
     * The service must validate the gateway signature or API result,
     * and confirm the order, bill and customer share one mess.
     */
    Optional<PaymentOrder> findByGatewayOrderId(
            String gatewayOrderId
    );

    /*
     * Worker lookup: job context comes from the persisted order.
     * Must be used inside a write transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT po
            FROM PaymentOrder po
            WHERE po.paymentOrderId = :paymentOrderId
            """)
    Optional<PaymentOrder> findByIdForUpdate(
            @Param("paymentOrderId") Long paymentOrderId
    );

    /*
     * Recover unfinished orders when their next check is due
     * and no worker holds an active lease.
     *
     * PAID and confirmed terminal orders are excluded.
     */
    @Query("""
            SELECT po.paymentOrderId
            FROM PaymentOrder po
            WHERE po.orderStatus IN (
                com.smartmess.backend.enums.PaymentOrderStatus.CREATING,
                com.smartmess.backend.enums.PaymentOrderStatus.ACTIVE,
                com.smartmess.backend.enums.PaymentOrderStatus.TERMINATION_REQUESTED,
                com.smartmess.backend.enums.PaymentOrderStatus.RECONCILIATION_REQUIRED
            )
              AND (
                  po.nextCheckAt IS NULL
                  OR po.nextCheckAt <= :now
              )
              AND (
                  po.leaseExpiresAt IS NULL
                  OR po.leaseExpiresAt <= :now
              )
            ORDER BY po.createdAt ASC, po.paymentOrderId ASC
            """)
    List<Long> findOrdersDueForVerification(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}