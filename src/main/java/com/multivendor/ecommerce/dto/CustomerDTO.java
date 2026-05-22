package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CustomerDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email required")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        private String address;
        private String city;
        private String pincode;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String pincode;

        public static Response from(Customer c) {
            return Response.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .email(c.getEmail())
                    .phone(c.getPhone())
                    .address(c.getAddress())
                    .city(c.getCity())
                    .pincode(c.getPincode())
                    .build();
        }
    }
}
