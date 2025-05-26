
package com.cheeza.Cheeza.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter
public class CreditCardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private String cardNumberMasked; // Don't store raw number! Mask except last 4 digits.

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private String expiry; // MM/YY, or consider using Year/Month ints.

    // Don't store CVV! Never persist CVV per PCI standards.

    @Column(nullable = false)
    private LocalDate addedOn = LocalDate.now();
}
