package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.PayoutDTO;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.OrderItem;
import com.multivendor.ecommerce.entity.Payout;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.PayoutRepository;
import com.multivendor.ecommerce.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutService {

    private static final BigDecimal COMMISSION_PERCENT = new BigDecimal("10.00");

    private final PayoutRepository payoutRepository;
    private final SellerRepository sellerRepository;

    /**
     * BUSINESS RULE: Payout is calculated ONLY after order is DELIVERED.
     * For each order item → calculate commission → net payout to seller.
     * Commission = 10% of item total
     * Net payout = item total - commission
     */
    @Transactional
    public void createPayoutsForOrder(Order order) {
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new BusinessException("Payouts can only be created for DELIVERED orders.");
        }

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal amount = item.getTotalPrice();
            BigDecimal commission = amount.multiply(COMMISSION_PERCENT)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal netPayout = amount.subtract(commission);

            Payout payout = Payout.builder()
                    .seller(item.getSeller())
                    .order(order)
                    .orderItem(item)
                    .amount(amount)
                    .commissionPct(COMMISSION_PERCENT)
                    .commissionAmt(commission)
                    .netPayout(netPayout)
                    .status(Payout.PayoutStatus.PENDING)
                    .build();

            payoutRepository.save(payout);
            log.info("Payout created: sellerId={}, orderId={}, netPayout={}",
                    item.getSeller().getId(), order.getId(), netPayout);
        }
    }

    @Transactional(readOnly = true)
    public List<PayoutDTO.Response> getAllPayouts() {
        return payoutRepository.findAll().stream()
                .map(PayoutDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayoutDTO.Response> getPayoutsBySeller(Long sellerId) {
        return payoutRepository.findBySellerId(sellerId).stream()
                .map(PayoutDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayoutDTO.Response> getPayoutsByStatus(Payout.PayoutStatus status) {
        return payoutRepository.findByStatus(status).stream()
                .map(PayoutDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayoutDTO.SellerSummary getSellerPayoutSummary(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));

        BigDecimal pending   = payoutRepository.getTotalPendingPayoutBySeller(sellerId);
        BigDecimal processed = payoutRepository.getTotalProcessedPayoutBySeller(sellerId);

        return PayoutDTO.SellerSummary.builder()
                .sellerId(sellerId)
                .sellerName(seller.getName())
                .totalPending(pending != null ? pending : BigDecimal.ZERO)
                .totalProcessed(processed != null ? processed : BigDecimal.ZERO)
                .build();
    }

    /**
     * Admin processes (disburses) a pending payout
     */
    @Transactional
    public PayoutDTO.Response processPayout(Long payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", payoutId));

        if (payout.getStatus() != Payout.PayoutStatus.PENDING) {
            throw new BusinessException("Only PENDING payouts can be processed. Current: " + payout.getStatus());
        }

        payout.setStatus(Payout.PayoutStatus.PROCESSED);
        payout.setProcessedAt(LocalDateTime.now());

        return PayoutDTO.Response.from(payoutRepository.save(payout));
    }
}
