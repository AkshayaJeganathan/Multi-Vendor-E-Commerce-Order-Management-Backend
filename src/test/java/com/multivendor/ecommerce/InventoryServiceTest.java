package com.multivendor.ecommerce;

import com.multivendor.ecommerce.dto.InventoryDTO;
import com.multivendor.ecommerce.entity.Inventory;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.InsufficientStockException;
import com.multivendor.ecommerce.repository.InventoryRepository;
import com.multivendor.ecommerce.repository.ProductRepository;
import com.multivendor.ecommerce.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Tests")
class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        Seller seller = Seller.builder().id(1L).name("Seller").build();
        product = Product.builder().id(1L).name("Widget")
                .price(new BigDecimal("100.00"))
                .status(Product.ProductStatus.ACTIVE)
                .seller(seller).build();

        inventory = Inventory.builder()
                .id(1L).product(product)
                .totalQuantity(50).reservedQuantity(10)
                .build();
    }

    @Test
    @DisplayName("Should fetch inventory by product")
    void testGetInventoryByProduct() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        InventoryDTO.Response resp = inventoryService.getInventoryByProduct(1L);

        assertThat(resp.getTotalQuantity()).isEqualTo(50);
        assertThat(resp.getReservedQuantity()).isEqualTo(10);
        assertThat(resp.getAvailableQuantity()).isEqualTo(40);
    }

    @Test
    @DisplayName("Should add stock successfully")
    void testAddStock_Success() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        inventory.setTotalQuantity(70);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO.Response resp = inventoryService.addStock(1L, 20);
        assertThat(resp.getTotalQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("Should throw on non-positive stock add")
    void testAddStock_InvalidQuantity() {
        assertThatThrownBy(() -> inventoryService.addStock(1L, 0))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when not enough stock")
    void testReserveStock_Insufficient() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        // available = 40, requesting 50
        assertThatThrownBy(() -> inventoryService.reserveStock(1L, 50))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should reserve stock when sufficient")
    void testReserveStock_Success() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.reserveStock(1L, 5)).thenReturn(1);

        assertThatCode(() -> inventoryService.reserveStock(1L, 5))
                .doesNotThrowAnyException();
    }
}
