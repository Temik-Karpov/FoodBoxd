package com.karpov.ru.foodboxd.model.entity;

import com.karpov.ru.foodboxd.model.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private ItemType entityType; // RESTAURANT или MENU_ITEM

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String url; // путь к файлу внутри uploads, например "/uploads/restaurant_1_photo_abc.jpg"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
