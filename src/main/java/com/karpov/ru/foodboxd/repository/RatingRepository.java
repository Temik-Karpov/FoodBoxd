package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с оценками пользователей.
 */
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Находит оценку конкретного пользователя для конкретного объекта (ресторана или блюда).
     *
     * @param userId   ID пользователя
     * @param itemType тип объекта
     * @param itemId   ID объекта
     * @return Optional с оценкой
     */
    Optional<Rating> findByUserIdAndRatedItemTypeAndRatedItemId(Long userId, ItemType itemType, Long itemId);

    /**
     * Возвращает все оценки, поставленные указанным пользователем.
     *
     * @param userId ID пользователя
     * @return список оценок
     */
    List<Rating> findAllByUserId(Long userId);

    /**
     * Вычисляет средний рейтинг для заданного объекта на основе всех его оценок.
     *
     * @param itemType тип объекта
     * @param itemId   ID объекта
     * @return среднее значение (может быть null, если нет оценок)
     */
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.ratedItemType = :itemType AND r.ratedItemId = :itemId")
    BigDecimal calculateAverageRating(@Param("itemType") ItemType itemType, @Param("itemId") Long itemId);

    /**
     * Подсчитывает количество оценок для указанного объекта.
     *
     * @param itemType тип объекта
     * @param itemId   ID объекта
     * @return количество оценок
     */
    long countByRatedItemTypeAndRatedItemId(ItemType itemType, Long itemId);

    /**
     * Возвращает последние оценки с комментариями для объекта.
     *
     * @param itemType тип объекта
     * @param itemId   идентификатор объекта
     * @param pageable ограничение количества
     * @return список оценок с непустыми комментариями
     */
    @Query("SELECT r FROM Rating r WHERE r.ratedItemType = :itemType AND r.ratedItemId = :itemId AND r.review IS NOT NULL AND r.review <> '' ORDER BY r.createdAt DESC")
    List<Rating> findReviewsByItem(@Param("itemType") ItemType itemType, @Param("itemId") Long itemId, Pageable pageable);

    /**
     * Считает количество комментариев для объекта.
     *
     * @param itemType тип объекта
     * @param itemId   идентификатор объекта
     * @return количество непустых комментариев
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedItemType = :itemType AND r.ratedItemId = :itemId AND r.review IS NOT NULL AND r.review <> ''")
    long countReviewsByItem(@Param("itemType") ItemType itemType, @Param("itemId") Long itemId);

    /**
     * Возвращает последние оценки указанных пользователей (для ленты подписок).
     *
     * @param userIds  ID пользователей
     * @param pageable ограничение количества
     * @return оценки, отсортированные по времени создания
     */
    @Query("SELECT r FROM Rating r WHERE r.user.id IN :userIds ORDER BY r.createdAt DESC")
    List<Rating> findRecentByUserIds(@Param("userIds") Collection<Long> userIds, Pageable pageable);
}
