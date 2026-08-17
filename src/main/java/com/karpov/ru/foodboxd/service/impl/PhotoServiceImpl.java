package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.model.entity.Photo;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.repository.PhotoRepository;
import com.karpov.ru.foodboxd.service.PhotoService;
import com.karpov.ru.foodboxd.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Реализация сервиса фотографий.
 */
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public Photo uploadPhoto(MultipartFile file, ItemType entityType, Long entityId) {
        String subDir = entityType.name().toLowerCase(); // "restaurant" или "menu_item"
        String filePath = storageService.store(file, subDir);

        Photo photo = Photo.builder()
                .entityType(entityType)
                .entityId(entityId)
                .url("/uploads/" + filePath) // URL для доступа через веб
                .build();

        return photoRepository.save(photo);
    }

    @Override
    @Transactional
    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Фото не найдено"));
        // Удаляем физический файл (путь уже относительный "restaurant/имя.jpg")
        storageService.delete(photo.getUrl().replace("/uploads/", ""));
        photoRepository.delete(photo);
    }

    @Override
    public List<Photo> getPhotos(ItemType entityType, Long entityId) {
        return photoRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
