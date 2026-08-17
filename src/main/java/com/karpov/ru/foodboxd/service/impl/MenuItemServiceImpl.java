package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.dto.MenuItemDto;
import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.repository.MenuItemRepository;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса позиций меню.
 */
@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public MenuItem create(Long restaurantId, MenuItemDto dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Ресторан с ID " + restaurantId + " не найден"));

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .weightGrams(dto.getWeightGrams())
                .isAlcoholic(dto.isAlcoholic())
                .alcoholPercentage(dto.getAlcoholPercentage())
                .imageUrl(dto.getImageUrl())
                .build();

        return menuItemRepository.save(item);
    }

    @Override
    @Transactional
    public MenuItem update(Long id, MenuItemDto dto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Позиция меню с ID " + id + " не найдена"));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setWeightGrams(dto.getWeightGrams());
        item.setAlcoholic(dto.isAlcoholic());
        item.setAlcoholPercentage(dto.getAlcoholPercentage());

        if (dto.getImageUrl() != null) {
            item.setImageUrl(dto.getImageUrl());
        }

        return menuItemRepository.save(item);
    }

    @Override
    @Transactional
    public void hide(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Позиция меню с ID " + id + " не найдена"));
        item.setAvailable(false);
        menuItemRepository.save(item);
    }

    @Override
    public List<MenuItem> findAvailableByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    @Override
    public List<MenuItem> findAllByRestaurant(Long restaurantId) {
        return menuItemRepository.findAllByRestaurantId(restaurantId);
    }

    @Override
    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Позиция меню с ID " + id + " не найдена"));
    }

    @Override
    @Transactional
    public void updateAverageRating(Long menuItemId, Double avgRating) {
        menuItemRepository.findById(menuItemId).ifPresent(item -> {
            item.setAverageRating(avgRating);
            menuItemRepository.save(item);
        });
    }

    @Override
    public List<MenuItem> searchByName(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return menuItemRepository.findByIsAvailableTrueAndNameContainingIgnoreCase(query)
                .stream()
                .limit(limit)
                .toList();
    }
}