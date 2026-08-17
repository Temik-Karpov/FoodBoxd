package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.dto.MenuItemDto;
import com.karpov.ru.foodboxd.model.entity.MenuItem;

import java.util.List;

/**
 * Сервис для управления позициями меню (блюдами и напитками).
 */
public interface MenuItemService {

    /**
     * Добавляет новую позицию в меню ресторана.
     * @param restaurantId идентификатор ресторана
     * @param dto данные позиции
     * @return созданная позиция меню
     */
    MenuItem create(Long restaurantId, MenuItemDto dto);

    /**
     * Обновляет существующую позицию меню.
     * @param id идентификатор позиции
     * @param dto новые данные
     * @return обновлённая позиция меню
     */
    MenuItem update(Long id, MenuItemDto dto);

    /**
     * Скрывает позицию меню (мягкое удаление). Оценки пользователей сохраняются.
     * @param id идентификатор позиции
     */
    void hide(Long id);

    /**
     * Возвращает все доступные позиции меню для указанного ресторана.
     * @param restaurantId идентификатор ресторана
     * @return список доступных позиций
     */
    List<MenuItem> findAvailableByRestaurant(Long restaurantId);

    /**
     * Возвращает все позиции меню (включая скрытые) для указанного ресторана.
     * Используется в административной панели.
     * @param restaurantId идентификатор ресторана
     * @return полный список позиций
     */
    List<MenuItem> findAllByRestaurant(Long restaurantId);

    /**
     * Находит позицию меню по идентификатору.
     * @param id идентификатор позиции
     * @return позиция меню
     * @throws IllegalArgumentException если позиция не найдена
     */
    MenuItem findById(Long id);

    /**
     * Обновляет средний рейтинг позиции меню. Вызывается при добавлении или изменении оценки.
     * @param menuItemId идентификатор позиции
     * @param avgRating новое значение среднего рейтинга (может быть null)
     */
    void updateAverageRating(Long menuItemId, Double avgRating);

    /**
     * Поиск доступных позиций меню по названию (содержит строку, без учёта регистра).
     * @param query поисковый запрос
     * @param limit максимальное количество результатов
     * @return список подходящих позиций
     */
    List<MenuItem> searchByName(String query, int limit);
}