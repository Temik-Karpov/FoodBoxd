package com.karpov.ru.foodboxd.service.impl;

import com.karpov.ru.foodboxd.model.entity.Follow;
import com.karpov.ru.foodboxd.model.entity.User;
import com.karpov.ru.foodboxd.repository.FollowRepository;
import com.karpov.ru.foodboxd.repository.UserRepository;
import com.karpov.ru.foodboxd.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса подписок.
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Нельзя подписаться на самого себя");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return;
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        followRepository.save(Follow.builder()
                .follower(follower)
                .following(following)
                .build());
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .ifPresent(followRepository::delete);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public long countFollowers(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    public long countFollowing(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    public List<User> getFollowing(Long followerId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(followerId).stream()
                .map(Follow::getFollowing)
                .toList();
    }

    @Override
    public List<Long> getFollowingIds(Long followerId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(followerId).stream()
                .map(follow -> follow.getFollowing().getId())
                .toList();
    }

    @Override
    public List<User> getFollowers(Long userId) {
        return followRepository.findByFollowingIdOrderByCreatedAtDesc(userId).stream()
                .map(Follow::getFollower)
                .toList();
    }
}
