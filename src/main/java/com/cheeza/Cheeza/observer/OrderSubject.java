package com.cheeza.Cheeza.observer;

import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderStatus;

public interface OrderSubject {
    void registerObserver(OrderObserver observer);
   // void removeObserver(OrderObserver observer);
    void notifyObservers(Order order, OrderStatus orderStatus);
}
