package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.UserList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с пользовательскими списками (подборками).
 */
public interface UserListRepository extends JpaRepository<UserList, Long> {

    /**
     * Находит все публичные списки, принадлежащие указанному пользователю.
     * @param ownerId ID владельца
     * @return список публичных списков
     */
    List<UserList> findByOwnerIdAndIsPublicTrue(Long ownerId);

    /**
     * Возвращает все списки (включая приватные) для конкретного пользователя.
     * @param ownerId ID владельца
     * @return список всех списков пользователя
     */
    List<UserList> findAllByOwnerId(Long ownerId);
}