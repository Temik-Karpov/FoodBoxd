package com.karpov.ru.foodboxd.service;

import com.karpov.ru.foodboxd.model.entity.User;

import java.util.List;

/**
 * Сервис для управления подписками между пользователями.
 */
public interface FollowService {

    /**
     * Оформляет подписку пользователя на другого пользователя.
     * @param followerId ID подписчика
     * @param followingId ID того, на кого подписываются
     */
    void follow(Long followerId, Long followingId);

    /**
     * Отменяет подписку.
     * @param followerId ID подписчика
     * @param followingId ID того, на кого были подписаны
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * Проверяет, подписан ли пользователь на другого.
     * @param followerId ID подписчика
     * @param followingId ID того, на кого подписаны
     * @return true, если подписка существует
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * Считает подписчиков пользователя.
     * @param userId ID пользователя
     * @return количество подписчиков
     */
    long countFollowers(Long userId);

    /**
     * Считает, на скольких пользователей подписан пользователь.
     * @param userId ID пользователя
     * @return количество подписок
     */
    long countFollowing(Long userId);

    /**
     * Возвращает пользователей, на которых подписан данный пользователь.
     * @param followerId ID подписчика
     * @return список пользователей
     */
    List<User> getFollowing(Long followerId);

    /**
     * Возвращает ID пользователей, на которых подписан данный пользователь.
     * @param followerId ID подписчика
     * @return список ID
     */
    List<Long> getFollowingIds(Long followerId);

    /**
     * Возвращает подписчиков пользователя.
     * @param userId ID пользователя
     * @return список подписчиков
     */
    List<User> getFollowers(Long userId);
}
