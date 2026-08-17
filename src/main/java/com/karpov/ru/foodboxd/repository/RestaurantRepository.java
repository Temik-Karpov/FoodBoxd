package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с ресторанами и барами.
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Находит все активные рестораны с пагинацией.
     * @param pageable настройки пагинации и сортировки
     * @return страница активных заведений
     */
    Page<Restaurant> findAllByIsActiveTrue(Pageable pageable);

    /**
     * Поиск активных ресторанов, название которых содержит заданную строку (без учёта регистра).
     * @param name часть названия
     * @return список подходящих ресторанов
     */
    List<Restaurant> findByIsActiveTrueAndNameContainingIgnoreCase(String name);

    /**
     * Поиск одного активного ресторана по ID.
     * @param id идентификатор
     * @return Optional с рестораном
     */
    Optional<Restaurant> findByIdAndIsActiveTrue(Long id);

    /**
     * Получает список всех активных ресторанов, отсортированных по среднему рейтингу по убыванию.
     * @return отсортированный список
     */
    List<Restaurant> findAllByIsActiveTrueOrderByAverageRatingDesc();
}
