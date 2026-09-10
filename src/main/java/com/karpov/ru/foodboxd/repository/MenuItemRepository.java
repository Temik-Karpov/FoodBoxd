package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с позициями меню (блюдами и напитками).
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Возвращает все доступные позиции меню для указанного ресторана.
     * @param restaurantId идентификатор ресторана
     * @return список доступных позиций
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    /**
     * Возвращает все позиции меню (включая скрытые) для заданного ресторана.
     * Используется в админ-панели.
     * @param restaurantId идентификатор ресторана
     * @return полный список позиций
     */
    List<MenuItem> findAllByRestaurantId(Long restaurantId);

    /**
     * Поиск доступных позиций меню, название которых содержит заданную строку (без учёта регистра).
     * @param name часть названия
     * @return список подходящих позиций
     */
    List<MenuItem> findByIsAvailableTrueAndNameContainingIgnoreCase(String name);

    /**
     * Поиск доступных позиций меню конкретного ресторана по названию (без учёта регистра).
     * @param restaurantId идентификатор ресторана
     * @param name часть названия
     * @return список подходящих позиций
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrueAndNameContainingIgnoreCase(Long restaurantId, String name);
}