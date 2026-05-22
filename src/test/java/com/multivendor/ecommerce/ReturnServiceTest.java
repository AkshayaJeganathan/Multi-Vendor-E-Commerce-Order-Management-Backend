package com.multivendor.ecommerce;

import com.multivendor.ecommerce.dto.ReturnDTO;
import com.multivendor.ecommerce.entity.*;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.repository.OrderItemRepository;
import com.multivendor.ecommerce.repository.OrderRepository;
import com.multivendor.ecommerce.repository.PayoutRepository;
import com.multivendor.ecommerce.repository.ReturnRepository;
import com.multivendor.ecommerce.service.InventoryService;
import com.multivendor.ecommerce.service.ReturnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnService Tests")
class ReturnServiceTest {

    @Mock private ReturnRepository returnRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private InventoryService inventoryService;
    @Mock private PayoutRepository payoutRepository;

    @InjectMocks
    private ReturnService returnService;

    private Order order;
    private OrderItem orderItem;
    private Return returnRequest;
    private Product product;

    @BeforeEach
    void setUp() {
        Seller seller = Seller.builder().id(1L).name("Seller").build();
        Customer customer = Customer.builder().id(1L).name("Customer").build();
        product = Product.builder().id(1L).name("Widget")
                .price(new BigDecimal("500.00")).seller(seller).build();

        order = Order.builder().id(1L).orderNumber("ORD-001")
                .customer(customer).status(Order.OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("500.00")).build();

        orderItem = OrderItem.builder().id(1L).order(order).product(product)
                .seller(seller).quantity(1).unitPrice(new BigDecimal("500.00"))
                .totalPrice(new BigDecimal("500.00")).build();

        returnRequest = Return.builder().id(1L).order(order)
                .orderItem(orderItem).reason("Defective product")
                .status(Return.ReturnStatus.REQUESTED).build();
    }

    @Test
    @DisplayName("Should request return for a DELIVERED order")
    void testRequestReturn_Success() {
        ReturnDTO.Request req = new ReturnDTO.Request();
        req.setOrderId(1L);
        req.setOrderItemId(1L);
        req.setReason("Defective product");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(orderRepository.save(any())).thenReturn(order);
        when(returnRepository.save(any(Return.class))).thenReturn(returnRequest);

        ReturnDTO.Response response = returnService.requestReturn(req);

        assertThat(response.getReason()).isEqualTo("Defective product");
        assertThat(response.getStatus()).isEqualTo(Return.ReturnStatus.REQUESTED);
    }

    @Test
    @DisplayName("Should reject return request for non-delivered order")
    void testRequestReturn_NotDelivered() {
        order.setStatus(Order.OrderStatus.SHIPPED);
        ReturnDTO.Request req = new ReturnDTO.Request();
        req.setOrderId(1L);
        req.setOrderItemId(1L);
        req.setReason("reason");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> returnService.requestReturn(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    @DisplayName("Should approve return and restore inventory")
    void testApproveReturn_RestoresInventory() {
        when(returnRepository.findById(1L)).thenReturn(Optional.of(returnRequest));
        when(payoutRepository.findByOrderId(1L)).thenReturn(List.of());
        when(orderRepository.save(any())).thenReturn(order);
        when(returnRepository.save(any())).thenReturn(returnRequest);

        ReturnDTO.Response response = returnService.approveReturn(1L);

        verify(inventoryService).restoreStockOnReturn(product.getId(), 1);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should reject return")
    void testRejectReturn_Success() {
        when(returnRepository.findById(1L)).thenReturn(Optional.of(returnRequest));
        when(orderRepository.save(any())).thenReturn(order);
        returnRequest.setStatus(Return.ReturnStatus.REJECTED);
        when(returnRepository.save(any())).thenReturn(returnRequest);

        ReturnDTO.Response response = returnService.rejectReturn(1L);

        assertThat(response.getStatus()).isEqualTo(Return.ReturnStatus.REJECTED);
    }
}
