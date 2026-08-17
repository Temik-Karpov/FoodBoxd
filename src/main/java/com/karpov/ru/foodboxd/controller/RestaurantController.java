package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.dto.RestaurantDto;
import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Photo;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.service.MenuItemService;
import com.karpov.ru.foodboxd.service.PhotoService;
import com.karpov.ru.foodboxd.service.RatingService;
import com.karpov.ru.foodboxd.service.RestaurantService;
import com.karpov.ru.foodboxd.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для просмотра информации о ресторанах и отдельных позициях меню,
 * а также для выполнения административных действий над ними.
 */
@Controller
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final RatingService ratingService;
    private final PhotoService photoService;
    private final UserService userService;

    /**
     * Отображает детальную страницу ресторана с меню, фотографиями и отзывами.
     * @param id идентификатор ресторана
     * @param model модель представления
     * @param principal текущий аутентифицированный пользователь (может быть null)
     * @return имя шаблона restaurant/detail
     */
    @GetMapping("/restaurants/{id}")
    public String restaurantDetail(@PathVariable Long id, Model model, Principal principal) {
        Restaurant restaurant = restaurantService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ресторан с ID " + id + " не найден"));

        List<MenuItem> menuItems = menuItemService.findAvailableByRestaurant(id);
        List<Photo> photos = photoService.getPhotos(ItemType.RESTAURANT, id);

        List<Rating> reviews = ratingService.getReviews(ItemType.RESTAURANT, id, 5);
        long reviewCount = ratingService.getReviewCount(ItemType.RESTAURANT, id);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("photos", photos);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviewCount);

        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(user -> {
                ratingService.getUserRatingForItem(user.getId(), ItemType.RESTAURANT, id)
                        .ifPresent(rating -> model.addAttribute("userRating", rating));

                Map<Long, Rating> menuRatings = new HashMap<>();
                for (MenuItem item : menuItems) {
                    ratingService.getUserRatingForItem(user.getId(), ItemType.MENU_ITEM, item.getId())
                            .ifPresent(rating -> menuRatings.put(item.getId(), rating));
                }
                model.addAttribute("menuRatings", menuRatings);
            });
        }

        return "restaurant/detail";
    }

    /**
     * Отображает детальную страницу отдельного блюда или напитка с отзывами.
     * @param id идентификатор позиции меню
     * @param model модель представления
     * @param principal текущий аутентифицированный пользователь (может быть null)
     * @return имя шаблона restaurant/menu-item-detail
     */
    @GetMapping("/menu-item/{id}")
    public String menuItemDetail(@PathVariable Long id, Model model, Principal principal) {
        MenuItem item = menuItemService.findById(id);
        List<Photo> photos = photoService.getPhotos(ItemType.MENU_ITEM, id);

        List<Rating> reviews = ratingService.getReviews(ItemType.MENU_ITEM, id, 5);
        long reviewCount = ratingService.getReviewCount(ItemType.MENU_ITEM, id);

        model.addAttribute("item", item);
        model.addAttribute("photos", photos);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviewCount);

        if (principal != null) {
            userService.findByEmail(principal.getName()).ifPresent(user -> {
                ratingService.getUserRatingForItem(user.getId(), ItemType.MENU_ITEM, id)
                        .ifPresent(rating -> model.addAttribute("userRating", rating));
            });
        }

        return "restaurant/menu-item-detail";
    }

    /**
     * Отображает все комментарии для объекта.
     * @param itemType тип объекта (restaurant или menu_item)
     * @param itemId идентификатор объекта
     * @param model модель представления
     * @return имя шаблона restaurant/all-reviews
     */
    @GetMapping("/reviews/{itemType}/{itemId}")
    public String allReviews(@PathVariable String itemType,
                             @PathVariable Long itemId,
                             Model model) {
        ItemType type = ItemType.valueOf(itemType.toUpperCase());
        String itemName = "";
        String backLink = "";

        if (type == ItemType.RESTAURANT) {
            Restaurant r = restaurantService.findById(itemId).orElse(null);
            itemName = r != null ? r.getName() : "Ресторан";
            backLink = "/restaurants/" + itemId;
        } else {
            MenuItem mi = menuItemService.findById(itemId);
            itemName = mi.getName();
            backLink = "/menu-item/" + itemId;
        }

        List<Rating> reviews = ratingService.getReviews(type, itemId, Integer.MAX_VALUE);

        model.addAttribute("itemName", itemName);
        model.addAttribute("backLink", backLink);
        model.addAttribute("reviews", reviews);
        model.addAttribute("itemType", itemType);
        model.addAttribute("itemId", itemId);
        return "restaurant/all-reviews";
    }

    // ========== Административные действия с рестораном ==========

    /**
     * Отображает форму редактирования ресторана.
     * @param id идентификатор ресторана
     * @param model модель представления
     * @return имя шаблона admin/restaurant-form
     */
    @GetMapping("/restaurants/{id}/edit")
    public String editRestaurantForm(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ресторан с ID " + id + " не найден"));
        RestaurantDto dto = new RestaurantDto();
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setDescription(restaurant.getDescription());
        model.addAttribute("restaurant", dto);
        model.addAttribute("restaurantId", id);
        return "admin/restaurant-form";
    }

    /**
     * Обрабатывает обновление ресторана.
     * @param id идентификатор ресторана
     * @param dto данные ресторана
     * @param result результат валидации
     * @return редирект на страницу ресторана
     */
    @PostMapping("/restaurants/{id}/edit")
    public String updateRestaurant(@PathVariable Long id,
                                   @Valid @ModelAttribute("restaurant") RestaurantDto dto,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return "admin/restaurant-form";
        }
        restaurantService.update(id, dto);
        return "redirect:/restaurants/" + id;
    }

    /**
     * Деактивирует ресторан (мягкое удаление).
     * @param id идентификатор ресторана
     * @return редирект на главную страницу
     */
    @PostMapping("/restaurants/{id}/deactivate")
    public String deactivateRestaurant(@PathVariable Long id) {
        restaurantService.deactivate(id);
        return "redirect:/";
    }

    /**
     * Загружает фотографию для ресторана.
     * @param id идентификатор ресторана
     * @param photo файл изображения
     * @return редирект на страницу ресторана
     */
    @PostMapping("/restaurants/{id}/photos/upload")
    public String uploadRestaurantPhoto(@PathVariable Long id,
                                        @RequestParam("photo") MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            photoService.uploadPhoto(photo, ItemType.RESTAURANT, id);
        }
        return "redirect:/restaurants/" + id;
    }

    /**
     * Удаляет фотографию ресторана.
     * @param photoId идентификатор фотографии
     * @param restaurantId идентификатор ресторана
     * @return редирект на страницу ресторана
     */
    @PostMapping("/restaurants/{restaurantId}/photos/{photoId}/delete")
    public String deleteRestaurantPhoto(@PathVariable Long photoId,
                                        @PathVariable Long restaurantId) {
        photoService.deletePhoto(photoId);
        return "redirect:/restaurants/" + restaurantId;
    }

    // ========== Административные действия с позицией меню ==========

    /**
     * Скрывает позицию меню (мягкое удаление).
     * @param id идентификатор позиции меню
     * @return редирект на страницу ресторана
     */
    @PostMapping("/menu-item/{id}/hide")
    public String hideMenuItem(@PathVariable Long id) {
        MenuItem item = menuItemService.findById(id);
        Long restaurantId = item.getRestaurant().getId();
        menuItemService.hide(id);
        return "redirect:/restaurants/" + restaurantId;
    }

    /**
     * Загружает фотографию для позиции меню.
     * @param id идентификатор позиции меню
     * @param photo файл изображения
     * @return редирект на страницу блюда
     */
    @PostMapping("/menu-item/{id}/photos/upload")
    public String uploadMenuItemPhoto(@PathVariable Long id,
                                      @RequestParam("photo") MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            photoService.uploadPhoto(photo, ItemType.MENU_ITEM, id);
        }
        return "redirect:/menu-item/" + id;
    }

    /**
     * Удаляет фотографию позиции меню.
     * @param photoId идентификатор фотографии
     * @param menuItemId идентификатор позиции меню
     * @return редирект на страницу блюда
     */
    @PostMapping("/menu-item/{menuItemId}/photos/{photoId}/delete")
    public String deleteMenuItemPhoto(@PathVariable Long photoId,
                                      @PathVariable Long menuItemId) {
        photoService.deletePhoto(photoId);
        return "redirect:/menu-item/" + menuItemId;
    }
}