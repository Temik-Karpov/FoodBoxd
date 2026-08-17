package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.dto.RestaurantDto;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с ресторанами и барами.
 */
public interface RestaurantService {

    /**
     * Создаёт новый ресторан на основе DTO.
     * @param dto данные ресторана
     * @return сохранённая сущность
     */
    Restaurant create(RestaurantDto dto);

    /**
     * Обновляет существующий ресторан.
     * @param id идентификатор ресторана
     * @param dto новые данные
     * @return обновлённый ресторан
     */
    Restaurant update(Long id, RestaurantDto dto);

    /**
     * Деактивирует ресторан (мягкое удаление).
     * @param id идентификатор ресторана
     */
    void deactivate(Long id);

    /**
     * Возвращает активный ресторан по идентификатору.
     * @param id идентификатор
     * @return Optional с рестораном
     */
    Optional<Restaurant> findById(Long id);

    /**
     * Возвращает страницу активных ресторанов с пагинацией.
     * @param pageable настройки пагинации
     * @return страница ресторанов
     */
    Page<Restaurant> findAllActive(Pageable pageable);

    /**
     * Возвращает все рестораны (включая деактивированные). Используется в админ-панели.
     * @return полный список ресторанов
     */
    List<Restaurant> findAll();

    /**
     * Обновляет средний рейтинг ресторана.
     * @param restaurantId идентификатор ресторана
     * @param avgRating новое значение среднего рейтинга
     */
    void updateAverageRating(Long restaurantId, Double avgRating);

    /**
     * Поиск активных ресторанов по названию (содержит строку, без учёта регистра).
     * @param query поисковый запрос
     * @param limit максимальное количество результатов
     * @return список подходящих ресторанов
     */
    List<Restaurant> searchByName(String query, int limit);
}