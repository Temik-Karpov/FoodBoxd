package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.model.entity.*;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления персональными списками пользователя.
 */
@Controller
@RequestMapping("/lists")
@RequiredArgsConstructor
public class ListController {

    private final UserListService userListService;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final RatingService ratingService;

    /**
     * Отображает все списки текущего пользователя.
     * @param principal текущий пользователь
     * @param model модель представления
     * @return шаблон lists/index
     */
    @GetMapping
    public String myLists(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("lists", userListService.getAllLists(user.getId()));
        return "lists/index";
    }

    /**
     * Отображает форму создания нового списка.
     * @return шаблон lists/create
     */
    @GetMapping("/create")
    public String createListForm() {
        return "lists/create";
    }

    /**
     * Обрабатывает создание нового списка и перенаправляет на его страницу.
     * @param name название списка
     * @param description описание
     * @param isPublic признак публичности
     * @param itemType тип элементов списка
     * @param principal текущий пользователь
     * @return редирект на страницу созданного списка
     */
    @PostMapping("/create")
    public String createList(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam(defaultValue = "false") boolean isPublic,
                             @RequestParam ItemType itemType,
                             Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        UserList created = userListService.createList(user.getId(), name, description, isPublic, itemType);
        return "redirect:/lists/" + created.getId();
    }

    /**
     * Отображает детальную страницу списка с поиском элементов.
     * @param listId идентификатор списка
     * @param model модель представления
     * @param principal текущий пользователь
     * @return шаблон lists/view
     */
    @GetMapping("/{listId}")
    public String viewList(@PathVariable Long listId, Model model, Principal principal) {
        UserList list = userListService.getListById(listId);

        // Собираем названия и рейтинги для элементов
        List<Map<String, Object>> enrichedItems = new ArrayList<>();
        Long currentUserId = null;
        if (principal != null) {
            currentUserId = userService.findByEmail(principal.getName()).orElseThrow().getId();
        }

        for (UserListItem item : list.getItems()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("listItemId", item.getId());
            itemData.put("sortOrder", item.getSortOrder());

            if (item.getItemType() == ItemType.RESTAURANT) {
                Restaurant restaurant = restaurantService.findById(item.getItemId()).orElse(null);
                itemData.put("name", restaurant != null ? restaurant.getName() : "Удалённый ресторан");
                itemData.put("link", "/restaurants/" + item.getItemId());
                itemData.put("averageRating", restaurant != null ? restaurant.getAverageRating() : null);
                itemData.put("restaurantName", null);
            } else {
                MenuItem menuItem = menuItemService.findById(item.getItemId());
                itemData.put("name", menuItem.getName());
                itemData.put("link", "/menu-item/" + item.getItemId());
                itemData.put("averageRating", menuItem.getAverageRating());
                itemData.put("restaurantName", menuItem.getRestaurant().getName());
                itemData.put("restaurantLink", "/restaurants/" + menuItem.getRestaurant().getId());
            }

            // Оценка текущего пользователя
            if (currentUserId != null) {
                ratingService.getUserRatingForItem(currentUserId, item.getItemType(), item.getItemId())
                        .ifPresent(rating -> itemData.put("userRating", rating.getScore()));
            }

            enrichedItems.add(itemData);
        }

        model.addAttribute("list", list);
        model.addAttribute("enrichedItems", enrichedItems);
        return "lists/view";
    }

    /**
     * Поиск элементов для добавления в список (AJAX).
     * @param listId идентификатор списка
     * @param query поисковый запрос
     * @return JSON с результатами поиска
     */
    @GetMapping("/{listId}/search")
    @ResponseBody
    public ResponseEntity<?> searchItems(@PathVariable Long listId,
                                         @RequestParam String query) {
        UserList list = userListService.getListById(listId);

        if (list.getItemType() == ItemType.RESTAURANT) {
            List<Restaurant> restaurants = restaurantService.searchByName(query, 10);
            List<Map<String, Object>> results = restaurants.stream()
                    .map(r -> Map.<String, Object>of(
                            "id", r.getId(),
                            "name", r.getName(),
                            "address", r.getAddress() != null ? r.getAddress() : "",
                            "type", "RESTAURANT"
                    ))
                    .toList();
            return ResponseEntity.ok(results);
        } else {
            List<MenuItem> items = menuItemService.searchByName(query, 10);
            List<Map<String, Object>> results = items.stream()
                    .map(i -> Map.<String, Object>of(
                            "id", i.getId(),
                            "name", i.getName(),
                            "restaurantName", i.getRestaurant().getName(),
                            "price", i.getPrice().toString(),
                            "type", "MENU_ITEM"
                    ))
                    .toList();
            return ResponseEntity.ok(results);
        }
    }

    /**
     * Добавляет элемент в список (AJAX).
     * @param listId идентификатор списка
     * @param itemId идентификатор объекта
     * @return JSON с информацией о добавленном элементе
     */
    @PostMapping("/{listId}/add")
    @ResponseBody
    public ResponseEntity<?> addItem(@PathVariable Long listId,
                                     @RequestParam Long itemId) {
        UserList list = userListService.getListById(listId);
        ItemType itemType = list.getItemType();
        var item = userListService.addItem(listId, itemType, itemId, 0);

        return ResponseEntity.ok(Map.of(
                "id", item.getId(),
                "itemId", item.getItemId(),
                "itemType", item.getItemType().name(),
                "sortOrder", item.getSortOrder()
        ));
    }

    /**
     * Удаляет элемент из списка (AJAX).
     * @param listId идентификатор списка
     * @param itemId идентификатор элемента списка
     * @return JSON с подтверждением удаления
     */
    @DeleteMapping("/{listId}/remove/{itemId}")
    @ResponseBody
    public ResponseEntity<?> removeItem(@PathVariable Long listId,
                                        @PathVariable Long itemId) {
        userListService.removeItem(listId, itemId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Обновляет порядок элементов в списке после перетаскивания.
     * @param listId идентификатор списка
     * @param itemIds упорядоченный список идентификаторов элементов
     * @return JSON с подтверждением
     */
    @PostMapping("/{listId}/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderItems(@PathVariable Long listId,
                                          @RequestBody List<Long> itemIds) {
        userListService.reorderItems(listId, itemIds);
        return ResponseEntity.ok(Map.of("success", true));
    }
}