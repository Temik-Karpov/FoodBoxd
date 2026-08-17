package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.service.ExternalDataProvider;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Заглушка внешнего провайдера. Возвращает пустой список.
 */
@Service
public class StubExternalDataProvider implements ExternalDataProvider {

    @Override
    public List<Restaurant> fetchRestaurants() {
        return Collections.emptyList();
    }
}
