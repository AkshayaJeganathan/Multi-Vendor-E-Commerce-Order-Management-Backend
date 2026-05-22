package com.multivendor.ecommerce;

import com.multivendor.ecommerce.dto.SellerDTO;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.SellerRepository;
import com.multivendor.ecommerce.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerService Tests")
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerService sellerService;

    private Seller mockSeller;
    private SellerDTO.Request request;

    @BeforeEach
    void setUp() {
        mockSeller = Seller.builder()
                .id(1L)
                .name("Test Seller")
                .email("test@seller.com")
                .phone("9876543210")
                .gstNumber("GST123")
                .address("123 Test Street")
                .status(Seller.SellerStatus.ACTIVE)
                .build();

        request = new SellerDTO.Request();
        request.setName("Test Seller");
        request.setEmail("test@seller.com");
        request.setPhone("9876543210");
        request.setGstNumber("GST123");
        request.setAddress("123 Test Street");
    }

    @Test
    @DisplayName("Should register a new seller successfully")
    void testRegisterSeller_Success() {
        when(sellerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(sellerRepository.save(any(Seller.class))).thenReturn(mockSeller);

        SellerDTO.Response response = sellerService.registerSeller(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@seller.com");
        assertThat(response.getName()).isEqualTo("Test Seller");
        verify(sellerRepository, times(1)).save(any(Seller.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when email already exists")
    void testRegisterSeller_DuplicateEmail() {
        when(sellerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> sellerService.registerSeller(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(sellerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return all sellers")
    void testGetAllSellers() {
        when(sellerRepository.findAll()).thenReturn(List.of(mockSeller));

        List<SellerDTO.Response> sellers = sellerService.getAllSellers();

        assertThat(sellers).hasSize(1);
        assertThat(sellers.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return seller by ID")
    void testGetSellerById_Found() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(mockSeller));

        SellerDTO.Response response = sellerService.getSellerById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Seller");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when seller not found")
    void testGetSellerById_NotFound() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSellerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should update seller status")
    void testUpdateSellerStatus() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(mockSeller));
        mockSeller.setStatus(Seller.SellerStatus.INACTIVE);
        when(sellerRepository.save(any(Seller.class))).thenReturn(mockSeller);

        SellerDTO.Response response = sellerService.updateSellerStatus(1L, Seller.SellerStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(Seller.SellerStatus.INACTIVE);
    }
}
