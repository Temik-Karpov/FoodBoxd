package com.karpov.ru.foodboxd.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис для сохранения и удаления файлов (изображений).
 */
public interface StorageService {

    /**
     * Сохраняет файл и возвращает относительный путь к нему.
     * @param file загруженный файл
     * @param subDir поддиректория внутри хранилища (например, "restaurants" или "menu")
     * @return путь к файлу относительно корня хранилища
     */
    String store(MultipartFile file, String subDir);

    /**
     * Удаляет файл по относительному пути.
     * @param filePath путь к файлу
     */
    void delete(String filePath);
}
