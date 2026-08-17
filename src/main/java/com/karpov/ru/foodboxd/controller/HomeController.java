package com.karpov.ru.foodboxd.controller;

import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Главная страница со списком ресторанов.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final RestaurantService restaurantService;

    /**
     * Главная страница с пагинированным списком активных ресторанов.
     * @param page номер страницы
     * @param size размер страницы
     * @param model модель
     * @return шаблон index
     */
    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("averageRating").descending());
        Page<Restaurant> restaurants = restaurantService.findAllActive(pageRequest);
        model.addAttribute("restaurants", restaurants);
        return "index";
    }
}