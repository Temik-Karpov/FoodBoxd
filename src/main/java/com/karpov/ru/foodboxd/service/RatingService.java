package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.dto.FeedItemDto;
import com.karpov.ru.foodboxd.dto.UserRatingDto;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.enums.ItemType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с оценками пользователей.
 */
public interface RatingService {

    /**
     * Сохраняет или обновляет оценку пользователя для указанного объекта.
     *
     * @param userId   ID пользователя
     * @param itemType тип объекта
     * @param itemId   ID объекта
     * @param score    оценка (0.5 – 5.0), обязательна
     * @param review   текстовый отзыв (опционально)
     * @return сохранённая оценка
     */
    Rating rate(Long userId, ItemType itemType, Long itemId, BigDecimal score, String review);

    /**
     * Возвращает все оценки пользователя.
     *
     * @param userId ID пользователя
     * @return список оценок
     */
    List<Rating> getUserRatings(Long userId);

    /**
     * Находит оценку конкретного пользователя для конкретного объекта.
     *
     * @param userId   ID пользователя
     * @param itemType тип объекта
     * @param itemId   ID объекта
     * @return Optional с оценкой
     */
    Optional<Rating> getUserRatingForItem(Long userId, ItemType itemType, Long itemId);

    /**
     * Возвращает средний рейтинг объекта (округлённый до 1 знака).
     *
     * @param itemType тип объекта
     * @param itemId   ID объекта
     * @return средний рейтинг или null, если нет оценок
     */
    Double getAverageRating(ItemType itemType, Long itemId);

    /**
     * Возвращает оценки пользователя с названиями объектов.
     *
     * @param userId ID пользователя
     * @return список DTO с названиями
     */
    List<UserRatingDto> getUserRatingsWithNames(Long userId);

    /**
     * Возвращает последние оценки и комментарии указанных пользователей для ленты.
     *
     * @param userIds ID пользователей
     * @param limit   максимальное количество записей
     * @return список элементов ленты
     */
    List<FeedItemDto> getFeed(List<Long> userIds, int limit);

    /**
     * Возвращает последние комментарии для объекта.
     *
     * @param itemType тип объекта
     * @param itemId   идентификатор объекта
     * @param limit    максимальное количество
     * @return список комментариев
     */
    List<Rating> getReviews(ItemType itemType, Long itemId, int limit);

    /**
     * Возвращает общее количество комментариев для объекта.
     *
     * @param itemType тип объекта
     * @param itemId   идентификатор объекта
     * @return количество комментариев
     */
    long getReviewCount(ItemType itemType, Long itemId);

    /**
     * Удаляет оценку/комментарий по идентификатору.
     *
     * @param ratingId идентификатор оценки
     */
    void deleteRating(Long ratingId);
}