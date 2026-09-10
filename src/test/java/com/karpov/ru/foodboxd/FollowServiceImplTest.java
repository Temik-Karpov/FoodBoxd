package com.karpov.ru.foodboxd;

import com.karpov.ru.foodboxd.model.entity.Follow;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.repository.FollowRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import com.karpov.ru.foodboxd.service.impl.FollowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link FollowServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowServiceImpl followService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        follower = new User();
        follower.setId(1L);
        follower.setUsername("alice");

        following = new User();
        following.setId(2L);
        following.setUsername("bob");
    }

    @Test
    void followShouldSaveNewFollow() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.save(any(Follow.class))).thenAnswer(inv -> inv.getArgument(0));

        followService.follow(1L, 2L);

        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(captor.capture());
        assertThat(captor.getValue().getFollower()).isSameAs(follower);
        assertThat(captor.getValue().getFollowing()).isSameAs(following);
    }

    @Test
    void followShouldNotDuplicateExistingFollow() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        followService.follow(1L, 2L);

        verify(followRepository, never()).save(any());
    }

    @Test
    void followShouldThrowWhenFollowingSelf() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Нельзя подписаться на самого себя");

        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollowShouldDeleteExistingFollow() {
        Follow follow = Follow.builder().follower(follower).following(following).build();
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(follow));

        followService.unfollow(1L, 2L);

        verify(followRepository).delete(follow);
    }

    @Test
    void unfollowShouldDoNothingWhenNotFollowing() {
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());

        followService.unfollow(1L, 2L);

        verify(followRepository, never()).delete(any());
    }

    @Test
    void isFollowingShouldReturnFalseForNullArguments() {
        assertThat(followService.isFollowing(null, 2L)).isFalse();
        assertThat(followService.isFollowing(1L, null)).isFalse();
        verifyNoInteractions(followRepository);
    }

    @Test
    void countsShouldDelegateToRepository() {
        when(followRepository.countByFollowingId(2L)).thenReturn(3L);
        when(followRepository.countByFollowerId(2L)).thenReturn(5L);

        assertThat(followService.countFollowers(2L)).isEqualTo(3L);
        assertThat(followService.countFollowing(2L)).isEqualTo(5L);
    }

    @Test
    void getFollowingIdsShouldReturnFollowingUsersIds() {
        Follow follow = Follow.builder().follower(follower).following(following).build();
        when(followRepository.findByFollowerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(follow));

        assertThat(followService.getFollowingIds(1L)).containsExactly(2L);
    }
}
