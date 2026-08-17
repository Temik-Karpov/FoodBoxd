package com.karpov.ru.foodboxd.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "weight_grams")
    private Integer weightGrams; // граммовка, может быть null для напитков

    @Column(name = "image_url")
    private String imageUrl; // ссылка на фото, загруженное через наше приложение

    @Builder.Default
    @Column(name = "is_alcoholic")
    private boolean isAlcoholic = false;

    @Column(name = "alcohol_percentage")
    private BigDecimal alcoholPercentage; // крепость в процентах, если алкогольный

    @Column(name = "average_rating")
    private Double averageRating;

    @Builder.Default
    @Column(name = "is_available")
    private boolean isAvailable = true; // мягкое удаление

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
