package com.karpov.ru.foodboxd.dto;

import com.karpov.ru.foodboxd.model.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO элемента ленты — оценка или комментарий подписанного пользователя.
 */
@Data
@AllArgsConstructor
public class FeedItemDto {
    private Long ratingId;
    private LocalDateTime createdAt;
    private ItemType itemType;
    private Long itemId;
    private String itemName;
    private String itemLink;
    private String restaurantName;
    private String restaurantLink;
    private BigDecimal score;
    private String review;
    private Long authorId;
    private String authorUsername;
    private String authorEmail;
    private String authorAvatarUrl;
}
