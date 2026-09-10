package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.dto.UserRatingDto;
import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.repository.MenuItemRepository;
import com.karpov.ru.foodboxd.repository.RatingRepository;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import com.karpov.ru.foodboxd.service.MenuItemService;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса оценок.
 */
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional
    public Rating rate(Long userId, ItemType itemType, Long itemId, BigDecimal score, String review) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (score != null && (score.compareTo(BigDecimal.valueOf(0.5)) < 0 || score.compareTo(BigDecimal.valueOf(5.0)) > 0)) {
            throw new IllegalArgumentException("Оценка должна быть от 0.5 до 5.0");
        }

        Rating rating = ratingRepository
                .findByUserIdAndRatedItemTypeAndRatedItemId(userId, itemType, itemId)
                .orElseGet(() -> Rating.builder()
                        .user(user)
                        .ratedItemType(itemType)
                        .ratedItemId(itemId)
                        .build());

        if (score != null) {
            rating.setScore(score);
        }
        rating.setReview(review);
        Rating saved = ratingRepository.save(rating);

        BigDecimal newAvg = ratingRepository.calculateAverageRating(itemType, itemId);
        Double newAvgDouble = null;
        if (newAvg != null) {
            newAvg = newAvg.setScale(1, RoundingMode.HALF_UP);
            newAvgDouble = newAvg.doubleValue();
        }

        if (itemType == ItemType.RESTAURANT) {
            restaurantService.updateAverageRating(itemId, newAvgDouble);
        } else if (itemType == ItemType.MENU_ITEM) {
            menuItemService.updateAverageRating(itemId, newAvgDouble);
        }

        return saved;
    }

    @Override
    public List<Rating> getUserRatings(Long userId) {
        return ratingRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Rating> getUserRatingForItem(Long userId, ItemType itemType, Long itemId) {
        return ratingRepository.findByUserIdAndRatedItemTypeAndRatedItemId(userId, itemType, itemId);
    }

    @Override
    public Double getAverageRating(ItemType itemType, Long itemId) {
        BigDecimal avg = ratingRepository.calculateAverageRating(itemType, itemId);
        if (avg == null) return null;
        return avg.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public List<UserRatingDto> getUserRatingsWithNames(Long userId) {
        List<Rating> ratings = ratingRepository.findAllByUserId(userId);
        List<UserRatingDto> result = new ArrayList<>();

        for (Rating r : ratings) {
            String itemName = "";
            String restaurantName = null;
            Long restaurantId = null;

            if (r.getRatedItemType() == ItemType.RESTAURANT) {
                Restaurant restaurant = restaurantRepository.findById(r.getRatedItemId()).orElse(null);
                itemName = restaurant != null ? restaurant.getName() : "Удалённый ресторан";
                restaurantName = itemName;
                restaurantId = r.getRatedItemId();
            } else if (r.getRatedItemType() == ItemType.MENU_ITEM) {
                MenuItem menuItem = menuItemRepository.findById(r.getRatedItemId()).orElse(null);
                if (menuItem != null) {
                    itemName = menuItem.getName();
                    restaurantName = menuItem.getRestaurant().getName();
                    restaurantId = menuItem.getRestaurant().getId();
                } else {
                    itemName = "Удалённое блюдо";
                }
            }

            result.add(new UserRatingDto(
                    r.getId(),
                    r.getRatedItemType(),
                    r.getRatedItemId(),
                    itemName,
                    r.getScore(),
                    r.getReview(),
                    restaurantName,
                    restaurantId
            ));
        }
        return result;
    }

    @Override
    public List<Rating> getReviews(ItemType itemType, Long itemId, int limit) {
        return ratingRepository.findReviewsByItem(itemType, itemId, PageRequest.of(0, limit));
    }

    @Override
    public long getReviewCount(ItemType itemType, Long itemId) {
        return ratingRepository.countReviewsByItem(itemType, itemId);
    }

    @Override
    @Transactional
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        ratingRepository.delete(rating);

        // Пересчёт среднего рейтинга
        ItemType itemType = rating.getRatedItemType();
        Long itemId = rating.getRatedItemId();
        BigDecimal newAvg = ratingRepository.calculateAverageRating(itemType, itemId);
        Double newAvgDouble = null;
        if (newAvg != null) {
            newAvg = newAvg.setScale(1, RoundingMode.HALF_UP);
            newAvgDouble = newAvg.doubleValue();
        }

        if (itemType == ItemType.RESTAURANT) {
            restaurantService.updateAverageRating(itemId, newAvgDouble);
        } else if (itemType == ItemType.MENU_ITEM) {
            menuItemService.updateAverageRating(itemId, newAvgDouble);
        }
    }
}