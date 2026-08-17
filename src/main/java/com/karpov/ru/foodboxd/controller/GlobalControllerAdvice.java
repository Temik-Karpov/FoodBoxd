package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/**
 * Добавляет глобальные атрибуты модели для всех страниц.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    /**
     * Добавляет URL аватара текущего пользователя в модель.
     */
    @ModelAttribute("currentUserAvatar")
    public String currentUserAvatar(Principal principal) {
        if (principal != null) {
            return userService.findByEmail(principal.getName())
                    .map(user -> user.getAvatarUrl() != null ? user.getAvatarUrl() : "/images/default-avatar.png")
                    .orElse("/images/default-avatar.png");
        }
        return "/images/default-avatar.png";
    }
}