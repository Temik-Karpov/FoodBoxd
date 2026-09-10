package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.dto.MenuItemDto;
import com.karpov.ru.foodboxd.model.entity.MenuItem;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.repository.MenuItemRepository;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.service.impl.MenuItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link MenuItemServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    @Test
    void createShouldAssociateMenuItemWithRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        MenuItemDto dto = new MenuItemDto();
        dto.setName("Burger");
        dto.setPrice(new BigDecimal("5.99"));
        dto.setWeightGrams(200);
        dto.setAlcoholic(false);

        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem created = menuItemService.create(1L, dto);

        assertThat(created.getRestaurant()).isEqualTo(restaurant);
        assertThat(created.getName()).isEqualTo("Burger");
        assertThat(created.getPrice()).isEqualByComparingTo("5.99");
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void hideShouldSetAvailableFalse() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setAvailable(true);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        menuItemService.hide(1L);

        assertThat(item.isAvailable()).isFalse();
        verify(menuItemRepository).save(item);
    }

    @Test
    void findAvailableByRestaurantShouldReturnOnlyAvailable() {
        when(menuItemRepository.findByRestaurantIdAndIsAvailableTrue(1L))
                .thenReturn(List.of(new MenuItem(), new MenuItem()));

        List<MenuItem> items = menuItemService.findAvailableByRestaurant(1L);
        assertThat(items).hasSize(2);
    }

    @Test
    void searchByRestaurantShouldReturnItemsOfThatRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        MenuItem item = MenuItem.builder()
                .name("Пицца Маргарита")
                .price(new BigDecimal("600"))
                .restaurant(restaurant)
                .build();
        when(menuItemRepository.findByRestaurantIdAndIsAvailableTrueAndNameContainingIgnoreCase(1L, "пицца"))
                .thenReturn(List.of(item));

        List<MenuItem> result = menuItemService.searchByRestaurant(1L, "пицца", 10);

        assertThat(result).containsExactly(item);
    }

    @Test
    void searchByRestaurantShouldLimitResults() {
        Restaurant restaurant = new Restaurant();
        List<MenuItem> many = java.util.stream.IntStream.range(0, 15)
                .mapToObj(i -> MenuItem.builder().name("Блюдо " + i).restaurant(restaurant).build())
                .toList();
        when(menuItemRepository.findByRestaurantIdAndIsAvailableTrueAndNameContainingIgnoreCase(1L, "блюдо"))
                .thenReturn(many);

        assertThat(menuItemService.searchByRestaurant(1L, "блюдо", 10)).hasSize(10);
    }

    @Test
    void searchByRestaurantShouldReturnEmptyForBlankQueryOrNullRestaurant() {
        assertThat(menuItemService.searchByRestaurant(1L, "   ", 10)).isEmpty();
        assertThat(menuItemService.searchByRestaurant(null, "пицца", 10)).isEmpty();
        verifyNoInteractions(menuItemRepository);
    }
}