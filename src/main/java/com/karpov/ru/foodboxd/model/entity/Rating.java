package com.karpov.ru.foodboxd.model.entity;

import com.karpov.ru.foodboxd.model.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "rated_item_type", "rated_item_id"})
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "rated_item_type", nullable = false, length = 20)
    private ItemType ratedItemType; // RESTAURANT или MENU_ITEM

    @Column(name = "rated_item_id", nullable = false)
    private Long ratedItemId; // ID ресторана или блюда

    @Column(precision = 2, scale = 1)
    private BigDecimal score; // от 0.5 до 5.0; null, если оставлен только текстовый отзыв

    @Column(length = 1000)
    private String review; // необязательный текстовый отзыв

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
