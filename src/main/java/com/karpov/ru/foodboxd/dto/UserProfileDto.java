package com.karpov.ru.foodboxd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO для редактирования профиля пользователя.
 */
@Data
public class UserProfileDto {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя должно быть от 3 до 50 символов")
    private String username;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String bio;

    @Size(max = 100, message = "Город не должен превышать 100 символов")
    private String city;

    private String currentAvatarUrl;

    private MultipartFile avatarFile;
}