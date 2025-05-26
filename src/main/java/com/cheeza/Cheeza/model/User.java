package com.cheeza.Cheeza.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;


    private String fullName;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role; // user/admin

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int loyaltyPoints = 0;



    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }

    public void deductLoyaltyPoints(int points) {
        this.loyaltyPoints = Math.max(0, this.loyaltyPoints - points);
    }

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Notification> notifications = new ArrayList<>();

    // Helper methods for bidirectional relationship
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setUser(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setUser(null);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Role getRole() {
        return role;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Required for Thymeleaf binding
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public void updateProfileDetails(String fullName, String phone, String address) {
        this.fullName = fullName != null ? fullName : this.fullName;
        this.phone = phone != null ? phone : this.phone;
        this.address = address != null ? address : this.address;
    }
    //No Arg Constructor
    protected User(){

    }

    private User(Builder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.fullName = builder.fullName;
        this.phone = builder.phone;
        this.address = builder.address;
        this.role = builder.role;
    
    }

    public static class Builder{
        private final String email;
        private final String password;
        private String fullName;
        private String phone;
        private String address;
        private Role role;
        private LocalDateTime createdAt;

        public Builder(String email,String password){
            this.email = email;
            this.password = password;
            this.role = Role.CUSTOMER;
        }


        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        public Builder phone(String phone){
            this.phone = phone;
            return this;
        }
        public Builder address(String address){
            this.address = address;
            return this;
        }
        public Builder role(Role role){
            this.role = role;
            return this;
        }
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public User build(){
            User user = new User(this);
            user.createdAt = this.createdAt != null ? this.createdAt : LocalDateTime.now();
            return user;
        }
    }




}
