package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.dto.RestaurantDto;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.service.impl.RestaurantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link RestaurantServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @Test
    void createShouldSaveRestaurantFromDto() {
        RestaurantDto dto = new RestaurantDto();
        dto.setName("Test Restaurant");
        dto.setAddress("123 Main St");
        dto.setDescription("Nice place");

        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

        Restaurant created = restaurantService.create(dto);

        assertThat(created.getName()).isEqualTo("Test Restaurant");
        assertThat(created.getAddress()).isEqualTo("123 Main St");
        assertThat(created.getDescription()).isEqualTo("Nice place");
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateShouldModifyExistingRestaurant() {
        Restaurant existing = Restaurant.builder()
                .name("Old Name")
                .address("Old Address")
                .description("Old Desc")
                .build();
        existing.setId(1L);

        RestaurantDto updateDto = new RestaurantDto();
        updateDto.setName("New Name");
        updateDto.setAddress("New Address");
        updateDto.setDescription("New Desc");

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(existing);

        Restaurant updated = restaurantService.update(1L, updateDto);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getAddress()).isEqualTo("New Address");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        verify(restaurantRepository).save(existing);
    }

    @Test
    void deactivateShouldSetActiveFalse() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setActive(true);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        restaurantService.deactivate(1L);

        assertThat(restaurant.isActive()).isFalse();
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void findByIdShouldReturnActiveRestaurant() {
        Restaurant active = new Restaurant();
        active.setId(1L);
        active.setActive(true);
        when(restaurantRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(active));

        Optional<Restaurant> result = restaurantService.findById(1L);
        assertThat(result).containsSame(active);
    }

    @Test
    void findAllShouldReturnAllRestaurantsIncludingInactive() {
        Restaurant inactive = new Restaurant();
        inactive.setActive(false);
        List<Restaurant> all = List.of(new Restaurant(), inactive);
        when(restaurantRepository.findAll()).thenReturn(all);

        List<Restaurant> result = restaurantService.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    void updateAverageRatingShouldSetRating() {
        Restaurant r = new Restaurant();
        r.setId(1L);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(r));

        restaurantService.updateAverageRating(1L, 4.5);

        assertThat(r.getAverageRating()).isEqualTo(4.5);
        verify(restaurantRepository).save(r);
    }
}