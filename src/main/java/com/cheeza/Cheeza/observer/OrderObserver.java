package com.cheeza.Cheeza.observer;

import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderStatus;

public interface OrderObserver {
    void update(Order order, OrderStatus orderStatus);
}
