package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.dto.RestaurantDto;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса ресторанов.
 */
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public Restaurant create(RestaurantDto dto) {
        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .build();
        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public Restaurant update(Long id, RestaurantDto dto) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ресторан с ID " + id + " не найден"));
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setDescription(dto.getDescription());
        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ресторан с ID " + id + " не найден"));
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    public Page<Restaurant> findAllActive(Pageable pageable) {
        return restaurantRepository.findAllByIsActiveTrue(pageable);
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    @Override
    @Transactional
    public void updateAverageRating(Long restaurantId, Double avgRating) {
        restaurantRepository.findById(restaurantId).ifPresent(r -> {
            r.setAverageRating(avgRating);
            restaurantRepository.save(r);
        });
    }

    @Override
    public List<Restaurant> searchByName(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return restaurantRepository.findByIsActiveTrueAndNameContainingIgnoreCase(query)
                .stream()
                .limit(limit)
                .toList();
    }
}