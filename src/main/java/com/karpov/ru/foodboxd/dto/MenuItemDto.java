package com.karpov.ru.foodboxd.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MenuItemDto {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    private Integer weightGrams;
    private boolean isAlcoholic;
    private BigDecimal alcoholPercentage;
    private String imageUrl; // может быть null при создании
}
