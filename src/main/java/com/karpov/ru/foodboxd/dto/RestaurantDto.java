package com.karpov.ru.foodboxd.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RestaurantDto {
    @NotBlank
    private String name;
    private String address;
    private String description;
}