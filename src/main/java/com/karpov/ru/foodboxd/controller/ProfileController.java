package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.dto.UserProfileDto;
import com.karpov.ru.foodboxd.dto.UserRatingDto;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.entity.UserList;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.UserListService;
import com.karpov.ru.foodboxd.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Контроллер профиля пользователя.
 */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final RatingService ratingService;
    private final UserListService userListService;

    /**
     * Просмотр публичного профиля пользователя по email.
     * @param email email пользователя
     * @param model модель
     * @return шаблон profile/view
     */
    @GetMapping("/profile/{email}")
    public String viewProfile(@PathVariable String email, Model model, Principal principal) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        List<UserRatingDto> ratings = ratingService.getUserRatingsWithNames(user.getId());

        // Публичные списки видны всем
        List<UserList> publicLists = userListService.getPublicLists(user.getId());

        // Все списки видны только владельцу
        boolean isOwner = principal != null && principal.getName().equals(email);
        if (isOwner) {
            model.addAttribute("allLists", userListService.getAllLists(user.getId()));
        }

        model.addAttribute("profileUser", user);
        model.addAttribute("ratings", ratings);
        model.addAttribute("publicLists", publicLists);
        model.addAttribute("isOwner", isOwner);
        return "profile/view";
    }

    /**
     * Форма редактирования собственного профиля.
     * @param principal текущий пользователь
     * @param model модель
     * @return шаблон profile/edit
     */
    @GetMapping("/profile/edit")
    public String editProfileForm(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();

        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setCity(user.getCity());
        dto.setCurrentAvatarUrl(user.getAvatarUrl());

        model.addAttribute("userProfile", dto);
        return "profile/edit";
    }

    /**
     * Обработка сохранения профиля.
     * @param dto данные формы
     * @param result результат валидации
     * @param principal текущий пользователь
     * @param model модель
     * @return редирект на профиль или форма с ошибками
     */
    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("userProfile") UserProfileDto dto,
                                BindingResult result,
                                Principal principal,
                                Model model) {
        if (result.hasErrors()) {
            return "profile/edit";
        }

        User user = userService.findByEmail(principal.getName()).orElseThrow();

        try {
            userService.updateProfile(user.getId(), dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            dto.setCurrentAvatarUrl(user.getAvatarUrl());
            return "profile/edit";
        }

        // Редирект на профиль, используя email (не меняется)
        return "redirect:/profile/" + user.getEmail();
    }
}