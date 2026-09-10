package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.entity.UserList;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.repository.MenuItemRepository;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.repository.UserListRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты поиска элементов списка: выбор ресторана и поиск блюд внутри него.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "listowner@example.com")
class ListControllerSearchTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserListRepository userListRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuItemRepository menuItemRepository;

    private UserList menuList;
    private Restaurant pizzeria;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(User.builder()
                .username("listowner")
                .email("listowner@example.com")
                .password("secret")
                .build());
        menuList = userListRepository.save(UserList.builder()
                .owner(owner)
                .name("Мои блюда")
                .isPublic(true)
                .itemType(ItemType.MENU_ITEM)
                .build());

        pizzeria = restaurantRepository.save(Restaurant.builder().name("Пиццерия").build());
        Restaurant sushi = restaurantRepository.save(Restaurant.builder().name("Суши-бар").build());

        menuItemRepository.save(MenuItem.builder()
                .restaurant(pizzeria).name("Пицца Маргарита").price(new BigDecimal("600")).build());
        menuItemRepository.save(MenuItem.builder()
                .restaurant(sushi).name("Пицца с лососем").price(new BigDecimal("900")).build());
    }

    @Test
    void searchWithRestaurantIdShouldReturnOnlyThatRestaurantItems() throws Exception {
        mockMvc.perform(get("/lists/" + menuList.getId() + "/search")
                        .param("query", "Пицца")
                        .param("restaurantId", String.valueOf(pizzeria.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Пицца Маргарита"))
                .andExpect(jsonPath("$[0].restaurantName").value("Пиццерия"));
    }

    @Test
    void searchWithoutRestaurantIdShouldReturnItemsFromAllRestaurants() throws Exception {
        mockMvc.perform(get("/lists/" + menuList.getId() + "/search")
                        .param("query", "Пицца"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void restaurantSearchShouldReturnMatchingRestaurants() throws Exception {
        mockMvc.perform(get("/lists/" + menuList.getId() + "/restaurants")
                        .param("query", "Суши"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Суши-бар"));
    }

    @Test
    void listViewForMenuItemListShouldRenderTwoStepSearch() throws Exception {
        mockMvc.perform(get("/lists/" + menuList.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Шаг 1. Найдите ресторан")))
                .andExpect(content().string(containsString("Шаг 2. Найдите блюдо или напиток")))
                .andExpect(content().string(containsString("restaurant-search-input")));
    }
}
