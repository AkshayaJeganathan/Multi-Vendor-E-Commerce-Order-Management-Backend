package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Seller;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class SellerDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        private String gstNumber;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String gstNumber;
        private String address;
        private Seller.SellerStatus status;
        private LocalDateTime createdAt;

        public static Response from(Seller seller) {
            return Response.builder()
                    .id(seller.getId())
                    .name(seller.getName())
                    .email(seller.getEmail())
                    .phone(seller.getPhone())
                    .gstNumber(seller.getGstNumber())
                    .address(seller.getAddress())
                    .status(seller.getStatus())
                    .createdAt(seller.getCreatedAt())
                    .build();
        }
    }
}
