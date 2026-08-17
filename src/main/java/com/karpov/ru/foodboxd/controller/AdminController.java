package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.dto.MenuItemDto;
import com.karpov.ru.foodboxd.dto.RestaurantDto;
import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.service.MenuItemService;
import com.karpov.ru.foodboxd.service.PhotoService;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Контроллер для административных действий: создание ресторанов и позиций меню,
 * удаление комментариев.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final PhotoService photoService;
    private final RatingService ratingService;

    // ======= Создание ресторана =======

    /**
     * Отображает форму создания нового ресторана.
     * @param model модель представления
     * @return имя шаблона admin/restaurant-form
     */
    @GetMapping("/restaurants/create")
    public String createRestaurantForm(Model model) {
        model.addAttribute("restaurant", new RestaurantDto());
        return "admin/restaurant-form";
    }

    /**
     * Обрабатывает создание нового ресторана.
     * @param dto данные ресторана
     * @param result результат валидации
     * @return редирект на страницу созданного ресторана
     */
    @PostMapping("/restaurants/create")
    public String createRestaurant(@Valid @ModelAttribute("restaurant") RestaurantDto dto,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return "admin/restaurant-form";
        }
        Restaurant created = restaurantService.create(dto);
        return "redirect:/restaurants/" + created.getId();
    }

    // ======= Управление меню =======

    /**
     * Отображает форму создания позиции меню для ресторана.
     * @param restaurantId идентификатор ресторана
     * @param model модель представления
     * @return имя шаблона admin/menu-form
     */
    @GetMapping("/restaurants/{restaurantId}/menu/create")
    public String createMenuItemForm(@PathVariable Long restaurantId, Model model) {
        model.addAttribute("menuItem", new MenuItemDto());
        model.addAttribute("restaurantId", restaurantId);
        return "admin/menu-form";
    }

    /**
     * Обрабатывает создание позиции меню.
     * @param restaurantId идентификатор ресторана
     * @param dto данные позиции меню
     * @param result результат валидации
     * @param photo загружаемое фото (может отсутствовать)
     * @return редирект на страницу ресторана
     */
    @PostMapping("/restaurants/{restaurantId}/menu/create")
    public String createMenuItem(@PathVariable Long restaurantId,
                                 @Valid @ModelAttribute("menuItem") MenuItemDto dto,
                                 BindingResult result,
                                 @RequestParam(required = false) MultipartFile photo) {
        if (result.hasErrors()) {
            return "admin/menu-form";
        }
        MenuItem item = menuItemService.create(restaurantId, dto);
        if (photo != null && !photo.isEmpty()) {
            photoService.uploadPhoto(photo, ItemType.MENU_ITEM, item.getId());
        }
        return "redirect:/restaurants/" + restaurantId;
    }

    // ======= Удаление комментариев =======

    /**
     * Удаляет комментарий по идентификатору оценки.
     * @param ratingId идентификатор оценки
     * @param referer заголовок Referer для возврата на предыдущую страницу
     * @return редирект на страницу, с которой пришёл запрос
     */
    @PostMapping("/reviews/{ratingId}/delete")
    public String deleteReview(@PathVariable Long ratingId,
                               @RequestHeader(value = "Referer", defaultValue = "/") String referer) {
        ratingService.deleteRating(ratingId);
        return "redirect:" + referer;
    }
}