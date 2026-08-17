package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.model.entity.Restaurant;

import java.util.List;

/**
 * Провайдер внешних данных для автоматического обновления информации о ресторанах.
 */
public interface ExternalDataProvider {

    /**
     * Получает список ресторанов из внешнего источника (заглушка).
     * @return список ресторанов (может быть пустым)
     */
    List<Restaurant> fetchRestaurants();
}