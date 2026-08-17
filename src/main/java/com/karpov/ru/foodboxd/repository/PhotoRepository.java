package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.Photo;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с фотографиями.
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    /**
     * Возвращает все фотографии, привязанные к указанной сущности (ресторану или блюду).
     * @param entityType тип сущности
     * @param entityId ID сущности
     * @return список фотографий
     */
    List<Photo> findByEntityTypeAndEntityId(ItemType entityType, Long entityId);
}
