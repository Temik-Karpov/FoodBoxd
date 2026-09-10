package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для обработки AJAX-запросов на выставление оценок.
 */
@Controller
@RequestMapping("/rate")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    /**
     * Устанавливает или изменяет оценку для объекта (ресторана или блюда).
     * @param itemType тип объекта (restaurant или menu_item)
     * @param itemId ID объекта
     * @param score оценка (0.5–5.0), необязательна — можно оставить только отзыв
     * @param review текстовый отзыв (необязательный)
     * @param principal текущий пользователь
     * @return JSON с оценкой пользователя и обновлённым средним рейтингом
     */
    @PostMapping("/{itemType}/{itemId}")
    @ResponseBody
    public ResponseEntity<?> rate(@PathVariable String itemType,
                                  @PathVariable Long itemId,
                                  @RequestParam(required = false) BigDecimal score,
                                  @RequestParam(required = false) String review,
                                  Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
        }

        if (score == null && (review == null || review.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Добавьте оценку или текст отзыва"));
        }

        ItemType type = ItemType.valueOf(itemType.toUpperCase());

        // principal.getName() теперь возвращает email
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Rating rating = ratingService.rate(user.getId(), type, itemId, score, review);
        Double avgRating = ratingService.getAverageRating(type, itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("userScore", rating.getScore());
        response.put("averageRating", avgRating);

        // Данные отзыва для мгновенного обновления списка на странице
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("score", rating.getScore());
        reviewData.put("review", rating.getReview());
        reviewData.put("username", user.getUsername());
        reviewData.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "/images/default-avatar.png");
        if (rating.getCreatedAt() != null) {
            reviewData.put("createdAt", rating.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        }
        response.put("review", reviewData);
        return ResponseEntity.ok(response);
    }
}