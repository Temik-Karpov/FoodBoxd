package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.dto.UserProfileDto;
import com.karpov.ru.foodboxd.dto.UserRegistrationDto;
import com.karpov.ru.foodboxd.model.entity.User;

import java.util.Optional;

/**
 * Сервис для управления пользователями.
 */
public interface UserService {

    /**
     * Регистрирует нового пользователя на основе DTO.
     * @param registrationDto данные регистрации
     * @return сохранённый пользователь
     */
    User register(UserRegistrationDto registrationDto);

    /**
     * Находит пользователя по имени.
     * @param username имя
     * @return Optional пользователя
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по email.
     * @param email email
     * @return Optional пользователя
     */
    Optional<User> findByEmail(String email);

    /**
     * Находит пользователя по ID.
     * @param id идентификатор
     * @return Optional пользователя
     */
    Optional<User> getUserById(Long id);

    /**
     * Обновляет профиль пользователя.
     * @param userId ID пользователя
     * @param dto данные профиля
     * @return обновлённый пользователь
     */
    User updateProfile(Long userId, UserProfileDto dto);

    /**
     * Инициирует сброс пароля — генерирует токен и отправляет на email.
     * @param email адрес пользователя
     */
    void initiatePasswordReset(String email);

    /**
     * Сбрасывает пароль по токену.
     * @param token токен сброса
     * @param newPassword новый пароль
     */
    void resetPassword(String token, String newPassword);
}