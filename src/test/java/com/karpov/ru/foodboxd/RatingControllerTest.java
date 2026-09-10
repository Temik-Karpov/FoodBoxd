package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.repository.MenuItemRepository;
import com.karpov.ru.foodboxd.repository.RatingRepository;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты endpoint'а оценок: оценка обязательна, комментарий — необязателен.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "reviewer@example.com")
class RatingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private RatingRepository ratingRepository;

    private User user;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("reviewer")
                .email("reviewer@example.com")
                .password("secret")
                .build());
        restaurant = restaurantRepository.save(Restaurant.builder()
                .name("Тестовый ресторан")
                .build());
        menuItem = menuItemRepository.save(MenuItem.builder()
                .restaurant(restaurant)
                .name("Тестовое блюдо")
                .price(new BigDecimal("199"))
                .build());
    }

    @Test
    void reviewWithoutScoreForMenuItemShouldBeRejected() throws Exception {
        mockMvc.perform(post("/rate/MENU_ITEM/" + menuItem.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("review", "Блюдо понравилось"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Поставьте оценку, чтобы отправить отзыв"));

        assertThat(ratingRepository.findByUserIdAndRatedItemTypeAndRatedItemId(
                user.getId(), ItemType.MENU_ITEM, menuItem.getId())).isEmpty();
    }

    @Test
    void reviewWithoutScoreForRestaurantShouldBeRejected() throws Exception {
        mockMvc.perform(post("/rate/RESTAURANT/" + restaurant.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("review", "Атмосфера отличная"))
                .andExpect(status().isBadRequest());

        assertThat(ratingRepository.findByUserIdAndRatedItemTypeAndRatedItemId(
                user.getId(), ItemType.RESTAURANT, restaurant.getId())).isEmpty();
    }

    @Test
    void scoreWithReviewShouldBeSaved() throws Exception {
        mockMvc.perform(post("/rate/MENU_ITEM/" + menuItem.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("score", "4.5")
                        .param("review", "Отличное блюдо"))
                .andExpect(status().isOk());

        Rating rating = ratingRepository
                .findByUserIdAndRatedItemTypeAndRatedItemId(user.getId(), ItemType.MENU_ITEM, menuItem.getId())
                .orElseThrow();
        assertThat(rating.getScore()).isEqualByComparingTo("4.5");
        assertThat(rating.getReview()).isEqualTo("Отличное блюдо");
    }

    @Test
    void scoreOnlyShouldStillWork() throws Exception {
        mockMvc.perform(post("/rate/RESTAURANT/" + restaurant.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("score", "4"))
                .andExpect(status().isOk());

        Rating rating = ratingRepository
                .findByUserIdAndRatedItemTypeAndRatedItemId(user.getId(), ItemType.RESTAURANT, restaurant.getId())
                .orElseThrow();
        assertThat(rating.getScore()).isEqualByComparingTo("4");
        assertThat(rating.getReview()).isNull();
    }
}
