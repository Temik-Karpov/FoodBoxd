package com.karpov.ru.foodboxd.repository;

import com.karpov.ru.foodboxd.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для работы с ролями пользователей.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по её строковому имени (например, "ROLE_ADMIN").
     * @param name имя роли
     * @return Optional с ролью, если найдена
     */
    Optional<Role> findByName(String name);
}
