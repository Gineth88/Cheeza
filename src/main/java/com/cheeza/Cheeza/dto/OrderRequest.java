package com.cheeza.Cheeza.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private List<OrderItemRequest> items;

    @NotBlank(message = "Name is required")
    private String customerName;

    @NotBlank(message = "Address is required")
    private String deliveryAddress;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;
//    private List<OrderItemRequest> items;
}

