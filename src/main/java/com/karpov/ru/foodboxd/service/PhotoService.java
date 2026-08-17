package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.model.entity.Photo;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Сервис для управления фотографиями сущностей.
 */
public interface PhotoService {

    /**
     * Загружает фотографию и привязывает её к указанной сущности.
     * @param file загружаемый файл
     * @param entityType тип сущности
     * @param entityId ID сущности
     * @return сохранённая запись Photo
     */
    Photo uploadPhoto(MultipartFile file, ItemType entityType, Long entityId);

    /**
     * Удаляет фотографию и её файл.
     * @param photoId ID фото
     */
    void deletePhoto(Long photoId);

    /**
     * Возвращает все фото для указанной сущности.
     * @param entityType тип сущности
     * @param entityId ID сущности
     * @return список фото
     */
    List<Photo> getPhotos(ItemType entityType, Long entityId);
}
