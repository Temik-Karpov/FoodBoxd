package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
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
     * @param score оценка (0.5–5.0)
     * @param review текстовый отзыв (необязательный)
     * @param principal текущий пользователь
     * @return JSON с оценкой пользователя и обновлённым средним рейтингом
     */
    @PostMapping("/{itemType}/{itemId}")
    @ResponseBody
    public ResponseEntity<?> rate(@PathVariable String itemType,
                                  @PathVariable Long itemId,
                                  @RequestParam BigDecimal score,
                                  @RequestParam(required = false) String review,
                                  Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
        }

        ItemType type = ItemType.valueOf(itemType.toUpperCase());

        // principal.getName() теперь возвращает email
        Long userId = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"))
                .getId();

        Rating rating = ratingService.rate(userId, type, itemId, score, review);
        Double avgRating = ratingService.getAverageRating(type, itemId);

        return ResponseEntity.ok(Map.of(
                "userScore", rating.getScore(),
                "averageRating", avgRating
        ));
    }
}