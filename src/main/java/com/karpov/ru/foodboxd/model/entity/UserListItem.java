package com.karpov.ru.foodboxd.model.entity;

import com.karpov.ru.foodboxd.model.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_list_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_list_id", "item_type", "item_id"})
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_list_id", nullable = false)
    private UserList userList;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
