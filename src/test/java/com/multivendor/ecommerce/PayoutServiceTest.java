package com.multivendor.ecommerce;

import com.multivendor.ecommerce.dto.PayoutDTO;
import com.multivendor.ecommerce.entity.*;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.repository.PayoutRepository;
import com.multivendor.ecommerce.repository.SellerRepository;
import com.multivendor.ecommerce.service.PayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayoutService Tests")
class PayoutServiceTest {

    @Mock private PayoutRepository payoutRepository;
    @Mock private SellerRepository sellerRepository;

    @InjectMocks
    private PayoutService payoutService;

    private Order order;
    private Seller seller;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        seller = Seller.builder().id(1L).name("Test Seller").build();

        Customer customer = Customer.builder().id(1L).name("Customer").build();

        Product product = Product.builder().id(1L).name("Widget")
                .price(new BigDecimal("500.00")).seller(seller).build();

        item = OrderItem.builder().id(1L).product(product).seller(seller)
                .quantity(2).unitPrice(new BigDecimal("500.00"))
                .totalPrice(new BigDecimal("1000.00")).build();

        order = Order.builder().id(1L).orderNumber("ORD-001")
                .customer(customer).status(Order.OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("1000.00"))
                .orderItems(List.of(item)).build();

        item.setOrder(order);
    }

    @Test
    @DisplayName("Should create payouts on order delivery with 10% commission")
    void testCreatePayoutsForOrder() {
        when(payoutRepository.save(any(Payout.class))).thenAnswer(inv -> inv.getArgument(0));

        payoutService.createPayoutsForOrder(order);

        verify(payoutRepository, times(1)).save(argThat(payout -> {
            // item total = 1000, commission = 100, net = 900
            assertThat(payout.getAmount()).isEqualByComparingTo("1000.00");
            assertThat(payout.getCommissionAmt()).isEqualByComparingTo("100.00");
            assertThat(payout.getNetPayout()).isEqualByComparingTo("900.00");
            assertThat(payout.getStatus()).isEqualTo(Payout.PayoutStatus.PENDING);
            return true;
        }));
    }

    @Test
    @DisplayName("Should fail creating payout for non-delivered order")
    void testCreatePayouts_NotDelivered() {
        order.setStatus(Order.OrderStatus.SHIPPED);

        assertThatThrownBy(() -> payoutService.createPayoutsForOrder(order))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    @DisplayName("Should process a PENDING payout")
    void testProcessPayout_Success() {
        Payout payout = Payout.builder().id(1L).seller(seller).order(order)
                .orderItem(item).amount(new BigDecimal("1000.00"))
                .commissionPct(new BigDecimal("10.00"))
                .commissionAmt(new BigDecimal("100.00"))
                .netPayout(new BigDecimal("900.00"))
                .status(Payout.PayoutStatus.PENDING).build();

        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));
        payout.setStatus(Payout.PayoutStatus.PROCESSED);
        when(payoutRepository.save(any())).thenReturn(payout);

        PayoutDTO.Response response = payoutService.processPayout(1L);
        assertThat(response.getStatus()).isEqualTo(Payout.PayoutStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should fail processing already-processed payout")
    void testProcessPayout_AlreadyProcessed() {
        Payout payout = Payout.builder().id(1L).status(Payout.PayoutStatus.PROCESSED).build();
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));

        assertThatThrownBy(() -> payoutService.processPayout(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("Should return seller payout summary")
    void testGetSellerSummary() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(payoutRepository.getTotalPendingPayoutBySeller(1L)).thenReturn(new BigDecimal("900.00"));
        when(payoutRepository.getTotalProcessedPayoutBySeller(1L)).thenReturn(new BigDecimal("1800.00"));

        PayoutDTO.SellerSummary summary = payoutService.getSellerPayoutSummary(1L);

        assertThat(summary.getTotalPending()).isEqualByComparingTo("900.00");
        assertThat(summary.getTotalProcessed()).isEqualByComparingTo("1800.00");
    }
}
