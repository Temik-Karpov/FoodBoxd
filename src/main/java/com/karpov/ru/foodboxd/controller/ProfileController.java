package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.dto.UserProfileDto;
import com.karpov.ru.foodboxd.dto.UserRatingDto;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.entity.UserList;
import com.karpov.ru.foodboxd.service.FollowService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер профиля пользователя.
 */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final RatingService ratingService;
    private final UserListService userListService;
    private final FollowService followService;

    /**
     * Каталог пользователей с поиском по имени или городу.
     * @param query поисковый запрос (опционально)
     * @param model модель
     * @param principal текущий пользователь
     * @return шаблон users/index
     */
    @GetMapping("/users")
    public String viewUsers(@RequestParam(value = "q", required = false) String query,
                            Model model,
                            Principal principal) {
        List<User> users = userService.searchUsers(query);

        Map<Long, Long> publicListCounts = new HashMap<>();
        for (User user : users) {
            publicListCounts.put(user.getId(), userListService.countPublicLists(user.getId()));
        }

        model.addAttribute("users", users);
        model.addAttribute("query", query);
        model.addAttribute("publicListCounts", publicListCounts);
        model.addAttribute("currentEmail", principal != null ? principal.getName() : null);
        return "users/index";
    }

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

        // Приватные списки видит только владелец, остальные — лишь публичные
        boolean isOwner = principal != null && principal.getName().equals(email);
        List<UserList> lists = isOwner
                ? userListService.getAllLists(user.getId())
                : userListService.getPublicLists(user.getId());

        Long currentUserId = principal != null
                ? userService.findByEmail(principal.getName()).map(User::getId).orElse(null)
                : null;

        model.addAttribute("profileUser", user);
        model.addAttribute("ratings", ratings);
        model.addAttribute("lists", lists);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("followerCount", followService.countFollowers(user.getId()));
        model.addAttribute("followingCount", followService.countFollowing(user.getId()));
        model.addAttribute("isFollowing", followService.isFollowing(currentUserId, user.getId()));
        return "profile/view";
    }

    /**
     * Оформляет подписку на пользователя.
     * @param email email пользователя
     * @param principal текущий пользователь
     * @return редирект на профиль
     */
    @PostMapping("/profile/{email}/follow")
    public String follow(@PathVariable String email, Principal principal) {
        User target = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        User current = userService.findByEmail(principal.getName()).orElseThrow();

        if (!current.getId().equals(target.getId())) {
            followService.follow(current.getId(), target.getId());
        }
        return "redirect:/profile/" + email;
    }

    /**
     * Отменяет подписку на пользователя.
     * @param email email пользователя
     * @param principal текущий пользователь
     * @return редирект на профиль
     */
    @PostMapping("/profile/{email}/unfollow")
    public String unfollow(@PathVariable String email, Principal principal) {
        User target = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        User current = userService.findByEmail(principal.getName()).orElseThrow();

        followService.unfollow(current.getId(), target.getId());
        return "redirect:/profile/" + email;
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