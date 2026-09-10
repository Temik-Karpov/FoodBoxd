package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.dto.FeedItemDto;
import com.karpov.ru.foodboxd.model.entity.Rating;
import com.karpov.ru.foodboxd.model.entity.Restaurant;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.model.enums.ItemType;
import com.karpov.ru.foodboxd.repository.MenuItemRepository;
import com.karpov.ru.foodboxd.repository.RatingRepository;
import com.karpov.ru.foodboxd.repository.RestaurantRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import com.karpov.ru.foodboxd.service.MenuItemService;
import com.karpov.ru.foodboxd.service.RestaurantService;
import com.karpov.ru.foodboxd.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
 * Unit-тесты для {@link RatingServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantService restaurantService;
    @Mock
    private MenuItemService menuItemService;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        // lenient() разрешает неиспользование заглушки в отдельных тестах
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void rateShouldCreateNewRatingAndUpdateAverage() {
        // given
        when(ratingRepository.findByUserIdAndRatedItemTypeAndRatedItemId(1L, ItemType.RESTAURANT, 10L))
                .thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ratingRepository.calculateAverageRating(ItemType.RESTAURANT, 10L))
                .thenReturn(new BigDecimal("4.50"));

        // when
        Rating result = ratingService.rate(1L, ItemType.RESTAURANT, 10L, new BigDecimal("4.5"), "Great");

        // then
        assertThat(result.getScore()).isEqualByComparingTo("4.5");
        assertThat(result.getReview()).isEqualTo("Great");
        verify(restaurantService).updateAverageRating(10L, 4.5);
    }

    @Test
    void rateShouldUpdateExistingRating() {
        Rating existing = Rating.builder()
                .user(user)
                .ratedItemType(ItemType.MENU_ITEM)
                .ratedItemId(20L)
                .score(new BigDecimal("3.0"))
                .build();

        when(ratingRepository.findByUserIdAndRatedItemTypeAndRatedItemId(1L, ItemType.MENU_ITEM, 20L))
                .thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(Rating.class))).thenReturn(existing);
        when(ratingRepository.calculateAverageRating(ItemType.MENU_ITEM, 20L))
                .thenReturn(new BigDecimal("4.0"));

        Rating result = ratingService.rate(1L, ItemType.MENU_ITEM, 20L, new BigDecimal("4.0"), null);

        assertThat(result.getScore()).isEqualByComparingTo("4.0");
        assertThat(result.getReview()).isNull();
        verify(menuItemService).updateAverageRating(20L, 4.0);
    }

    @Test
    void rateShouldThrowForInvalidScore() {
        assertThatThrownBy(() -> ratingService.rate(1L, ItemType.RESTAURANT, 1L, new BigDecimal("0.0"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Оценка должна быть от 0.5 до 5.0");

        assertThatThrownBy(() -> ratingService.rate(1L, ItemType.RESTAURANT, 1L, new BigDecimal("5.5"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rateShouldThrowWhenScoreIsNull() {
        assertThatThrownBy(() -> ratingService.rate(1L, ItemType.RESTAURANT, 1L, null, "текст отзыва"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Оценка обязательна");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void getUserRatingForItemShouldReturnFromRepository() {
        Rating rating = new Rating();
        when(ratingRepository.findByUserIdAndRatedItemTypeAndRatedItemId(1L, ItemType.RESTAURANT, 5L))
                .thenReturn(Optional.of(rating));

        Optional<Rating> result = ratingService.getUserRatingForItem(1L, ItemType.RESTAURANT, 5L);
        assertThat(result).containsSame(rating);
    }

    @Test
    void getAverageRatingShouldReturnNullWhenNoRatings() {
        when(ratingRepository.calculateAverageRating(ItemType.MENU_ITEM, 99L))
                .thenReturn(null);

        Double avg = ratingService.getAverageRating(ItemType.MENU_ITEM, 99L);
        assertThat(avg).isNull();
    }

    @Test
    void getFeedShouldReturnEmptyWhenNoUserIds() {
        assertThat(ratingService.getFeed(List.of(), 10)).isEmpty();
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void getFeedShouldMapRatingWithAuthorAndItem() {
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        Rating rating = Rating.builder()
                .id(100L)
                .user(user)
                .ratedItemType(ItemType.RESTAURANT)
                .ratedItemId(10L)
                .score(new BigDecimal("4.5"))
                .review("Отличное место")
                .build();
        when(ratingRepository.findRecentByUserIds(List.of(1L),
                org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(List.of(rating));

        Restaurant restaurant = new Restaurant();
        restaurant.setId(10L);
        restaurant.setName("Pizza House");
        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));

        List<FeedItemDto> feed = ratingService.getFeed(List.of(1L), 10);

        assertThat(feed).hasSize(1);
        FeedItemDto item = feed.get(0);
        assertThat(item.getItemName()).isEqualTo("Pizza House");
        assertThat(item.getItemLink()).isEqualTo("/restaurants/10");
        assertThat(item.getReview()).isEqualTo("Отличное место");
        assertThat(item.getScore()).isEqualByComparingTo("4.5");
        assertThat(item.getAuthorId()).isEqualTo(1L);
        assertThat(item.getAuthorUsername()).isEqualTo("alice");
        assertThat(item.getAuthorEmail()).isEqualTo("alice@example.com");
    }
}