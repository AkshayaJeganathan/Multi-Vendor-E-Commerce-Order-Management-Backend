package com.multivendor.ecommerce;

import com.multivendor.ecommerce.dto.OrderDTO;
import com.multivendor.ecommerce.entity.*;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.*;
import com.multivendor.ecommerce.service.InventoryService;
import com.multivendor.ecommerce.service.OrderService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryService inventoryService;
    @Mock private PayoutService payoutService;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private Seller seller;
    private Order order;

    @BeforeEach
    void setUp() {
        seller = Seller.builder().id(1L).name("Test Seller")
                .status(Seller.SellerStatus.ACTIVE).build();

        customer = Customer.builder().id(1L).name("Test Customer")
                .email("cust@test.com").phone("9000000001")
                .address("Test Address").build();

        product = Product.builder().id(1L).name("Test Product")
                .price(new BigDecimal("999.00"))
                .status(Product.ProductStatus.ACTIVE)
                .seller(seller).build();

        order = Order.builder().id(1L).orderNumber("ORD-TEST-001")
                .customer(customer).status(Order.OrderStatus.PLACED)
                .totalAmount(new BigDecimal("999.00"))
                .orderItems(new ArrayList<>()).build();
    }

    @Test
    @DisplayName("Should place order and reserve inventory")
    void testPlaceOrder_Success() {
        OrderDTO.PlaceRequest req = new OrderDTO.PlaceRequest();
        req.setCustomerId(1L);
        req.setShippingAddress("Test Address");
        OrderDTO.OrderItemRequest itemReq = new OrderDTO.OrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(2);
        req.setItems(List.of(itemReq));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(inventoryService).reserveStock(anyLong(), anyInt());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO.Response response = orderService.placeOrder(req);

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-TEST-001");
        verify(inventoryService, times(1)).reserveStock(1L, 2);
    }

    @Test
    @DisplayName("Should fail when customer not found")
    void testPlaceOrder_CustomerNotFound() {
        OrderDTO.PlaceRequest req = new OrderDTO.PlaceRequest();
        req.setCustomerId(99L);
        req.setItems(List.of());

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject order for inactive product")
    void testPlaceOrder_InactiveProduct() {
        product.setStatus(Product.ProductStatus.INACTIVE);
        OrderDTO.PlaceRequest req = new OrderDTO.PlaceRequest();
        req.setCustomerId(1L);
        OrderDTO.OrderItemRequest itemReq = new OrderDTO.OrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(1);
        req.setItems(List.of(itemReq));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Should update order status: PLACED → CONFIRMED")
    void testUpdateStatus_PlacedToConfirmed() {
        order.setOrderItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        order.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO.Response response = orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);
        assertThat(response.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should reject invalid status transition")
    void testUpdateStatus_InvalidTransition() {
        order.setStatus(Order.OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, Order.OrderStatus.DELIVERED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should release inventory on cancellation")
    void testCancelOrder_ReleasesInventory() {
        OrderItem item = OrderItem.builder().id(1L).product(product)
                .quantity(2).build();
        order.setOrderItems(List.of(item));
        order.setStatus(Order.OrderStatus.PLACED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        order.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);

        verify(inventoryService, times(1)).releaseReservation(product.getId(), 2);
    }
}
