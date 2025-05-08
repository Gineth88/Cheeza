package com.cheeza.Cheeza.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public Notification(String title, String message, LocalDateTime createdAt) {
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.read = false;
    }

    public Notification(){

    }


}
