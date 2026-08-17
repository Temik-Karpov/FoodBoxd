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
}