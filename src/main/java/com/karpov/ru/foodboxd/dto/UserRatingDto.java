package com.karpov.ru.foodboxd.dto;

import com.karpov.ru.foodboxd.model.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO для отображения оценки с названием объекта.
 */
@Data
@AllArgsConstructor
public class UserRatingDto {
    private Long ratingId;
    private ItemType itemType;
    private Long itemId;
    private String itemName;
    private BigDecimal score;
    private String review;
    private String restaurantName;
    private Long restaurantId;
}