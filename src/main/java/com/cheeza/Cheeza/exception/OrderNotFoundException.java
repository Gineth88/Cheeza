package com.cheeza.Cheeza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Order not found")
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order with ID " + orderId + " not found");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
