package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с подписками пользователей.
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * Проверяет, подписан ли пользователь на другого.
     * @param followerId ID подписчика
     * @param followingId ID того, на кого подписаны
     * @return true, если подписка существует
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * Находит подписку по подписчику и объекту подписки.
     * @param followerId ID подписчика
     * @param followingId ID того, на кого подписаны
     * @return Optional с подпиской
     */
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * Возвращает подписки пользователя (на кого он подписан).
     * @param followerId ID подписчика
     * @return список подписок
     */
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

    /**
     * Возвращает подписки на пользователя (его подписчиков).
     * @param followingId ID того, на кого подписаны
     * @return список подписок
     */
    List<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId);

    /**
     * Считает подписчиков пользователя.
     * @param followingId ID пользователя
     * @return количество подписчиков
     */
    long countByFollowingId(Long followingId);

    /**
     * Считает, на скольких пользователей подписан пользователь.
     * @param followerId ID пользователя
     * @return количество подписок
     */
    long countByFollowerId(Long followerId);
}
