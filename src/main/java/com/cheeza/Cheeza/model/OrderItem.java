package com.cheeza.Cheeza.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")  // This creates the foreign key column
    private Order order;

    @ManyToOne
    private Pizza pizza;

    private Integer quantity;
    private String specialInstruction;

    public OrderItem(Pizza pizza, int quantity) {
        this.pizza = pizza;
        this.quantity = quantity;
    }

    public OrderItem(Pizza pizza, int quantity, String specialInstructions, Order order) {
        this.pizza = pizza;
        this.quantity = quantity;
        this.specialInstruction = specialInstructions;
        this.order = order;
    }
}
