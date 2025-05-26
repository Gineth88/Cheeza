package com.cheeza.Cheeza.model;

import com.cheeza.Cheeza.observer.OrderObserver;
import com.cheeza.Cheeza.observer.OrderSubject;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pizza_orders")
@Data
public class Order implements OrderSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String deliveryAddress;
    private String phone;
    private String email;

    private LocalDateTime orderTime;
    private LocalDateTime estimatedDelivery;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Pizza> pizzas = new ArrayList<>();

    private double deliveryFee;
    private double discount;

    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // Creates foreign key in orders table
    private User user;

    private Instant lastUpdated;

    private transient List<OrderObserver> observers = new ArrayList<>();

    public void setStatus(OrderStatus newStatus){
        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        notifyObservers(this,oldStatus);

    }

    @Override
    public void registerObserver(OrderObserver observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers(Order order, OrderStatus oldStatus) {
        observers.forEach(observer -> observer.update(order, oldStatus));
    }


    public double calculateTotal() {
        double subtotal = pizzas.stream()
                .mapToDouble(Pizza::getPrice)
                .sum();

        double totalBeforeDiscount = subtotal + deliveryFee;

        if (discount > 0) {
            double discountAmount = totalBeforeDiscount * (discount / 100);
            return totalBeforeDiscount - discountAmount;
        }

        return totalBeforeDiscount;
    }
}